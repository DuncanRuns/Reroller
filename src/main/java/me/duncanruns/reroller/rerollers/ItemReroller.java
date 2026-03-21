package me.duncanruns.reroller.rerollers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class ItemReroller extends Reroller {
    protected Collection<ItemRequirementSet> requirementSets;

    @Override
    public boolean loadConfig(JsonObject configEntry) {
        if (!super.loadConfig(configEntry)) return false;
        if (!configEntry.has("requirements")) return false;
        JsonArray requirements = configEntry.get("requirements").getAsJsonArray();
        Collection<ItemRequirementSetData> requirementSets = StreamSupport.stream(requirements.spliterator(), false).map(e -> GSON.fromJson(e, ItemRequirementSetData.class)).collect(Collectors.toList());
        this.requirementSets = requirementSets.stream().map(ItemRequirementSet::new).collect(Collectors.toList());
        return true;
    }

    protected boolean test(MinecraftServer server, long seed) {
        Random random = new Random(seed);

        // Start roll -> (Item ID -> Count)
        Map<Integer, Map<Identifier, Integer>> itemCounts = new HashMap<>();
        requirementSets.forEach(s -> itemCounts.computeIfAbsent(s.rolls.start, i -> new HashMap<>()));
        int rolled = 0;
        for (ItemRequirementSet set : requirementSets.stream().sorted(Comparator.comparingInt(r -> r.rolls.end)).collect(Collectors.toCollection(ArrayList::new))) {
            if (rolled > set.rolls.end)
                throw new IllegalStateException("Item requirement sets iterated out of order!");
            while (rolled < set.rolls.end) {
                rolled++;
                for (ItemStack stack : getNext(server, random)) {
                    Identifier itemId = Registry.ITEM.getId(stack.getItem());
                    int stackCount = stack.getCount();
                    for (Map.Entry<Integer, Map<Identifier, Integer>> entry : itemCounts.entrySet()) {
                        if (rolled < entry.getKey()) continue;
                        entry.getValue().merge(itemId, stackCount, Integer::sum);
                    }
                }
            }

            for (Map.Entry<Identifier, ItemRequirementSet.CountRange> entry : set.items.entrySet()) {
                Identifier itemId = entry.getKey();
                int min = entry.getValue().min;
                int max = entry.getValue().max;
                int count = itemCounts.get(set.rolls.start).getOrDefault(itemId, 0);
                if (count < min || count > max) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Gets the next roll of the loot table. Should probably be overridden by implementations.
     */
    protected abstract Collection<ItemStack> getNext(MinecraftServer server, Random random);

    protected static class ItemRequirementSetData {
        public Map<String, String> items;
        public String rolls;
    }

    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)\\.\\.(\\d+)");
    private static final Pattern MAX_RANGE_PATTERN = Pattern.compile("\\.\\.(\\d+)");
    private static final Pattern MIN_RANGE_PATTERN = Pattern.compile("(\\d+)\\.\\.");
    private static final Pattern ALONE_PATTERN = Pattern.compile("(\\d+)");


    protected static class ItemRequirementSet {
        public Map<Identifier, CountRange> items;
        public RollRange rolls;

        public ItemRequirementSet(ItemRequirementSetData data) throws IllegalArgumentException {
            this.items = data.items.entrySet().stream().collect(Collectors.toMap(e -> new Identifier(e.getKey()), e -> new CountRange(e.getValue())));
            this.rolls = new RollRange(data.rolls);
        }

        protected static class CountRange {
            public int min;
            public int max;

            public CountRange(String countStr) throws IllegalArgumentException {
                // "1..5" -> min = 1, max = 5
                // "1" -> min = 1, max = MAX_VALUE
                // "..3" -> min = 0, max = 3
                // "3.." -> min = 3, max = MAX_VALUE
                Matcher matcher;
                if ((matcher = RANGE_PATTERN.matcher(countStr)).matches()) {
                    min = Integer.parseInt(matcher.group(1));
                    max = Integer.parseInt(matcher.group(2));
                } else if ((matcher = MAX_RANGE_PATTERN.matcher(countStr)).matches()) {
                    min = 0;
                    max = Integer.parseInt(matcher.group(1));
                } else if ((matcher = MIN_RANGE_PATTERN.matcher(countStr)).matches()
                        || (matcher = ALONE_PATTERN.matcher(countStr)).matches()) {
                    min = Integer.parseInt(matcher.group(1));
                    max = Integer.MAX_VALUE;
                } else {
                    throw new IllegalArgumentException("Invalid count range: " + countStr);
                }
            }
        }

        protected static class RollRange {
            public int start;
            public int end;

            public RollRange(String rollStr) throws IllegalArgumentException {
                // "1..5" -> start = 1, end = 5
                // "4" -> start = 1, end = 4
                // "..3" -> start = 1, end = 3
                // "3.." INVALID
                Matcher matcher;
                if ((matcher = RANGE_PATTERN.matcher(rollStr)).matches()) {
                    start = Integer.parseInt(matcher.group(1));
                    end = Integer.parseInt(matcher.group(2));
                } else if ((matcher = MAX_RANGE_PATTERN.matcher(rollStr)).matches()
                        || (matcher = ALONE_PATTERN.matcher(rollStr)).matches()) {
                    start = 1;
                    end = Integer.parseInt(matcher.group(1));
                } else {
                    throw new IllegalArgumentException("Invalid count range: " + rollStr);
                }
            }
        }
    }
}
