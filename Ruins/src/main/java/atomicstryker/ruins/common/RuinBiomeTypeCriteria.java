package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

// a biome type criteria collection against which biomes are checked
// as to whether or not they satisfy at least one of the specified
// conditions
class RuinBiomeTypeCriteria
{
    private RuinTemplate template_;
    private PrintWriter log_;

    // a biome satisfies a particular criterion if it is assigned
    // ALL the included types and NONE of the excluded ones
    private static class Criterion
    {
        private RuinTemplate template_;
        private PrintWriter log_;

        private Set<String> included_;
        private Set<String> excluded_;

        private static final Pattern SPEC_PATTERN = Pattern.compile("(?:\\+|(-))?+([^+-]++)");

        // parse given specification string into a new criterion
        public Criterion(RuinTemplate template, PrintWriter log, String spec)
        {
            template_ = template;
            log_ = log;
            if (log_ != null)
            {
                log_.printf("adding new criterion: spec=\"%s\"\n", spec);
            }
            included_ = new HashSet<>();
            excluded_ = new HashSet<>();
            Matcher matcher = SPEC_PATTERN.matcher(spec);
            int start = 0;
            final int end = spec.length();
            while (start < end)
            {
                if (matcher.find(start))
                {
                    if (matcher.start() != start)
                    {
                        System.err.printf("invalid use of operator(s) in biome type list; template=\"%s\", list element=\"%s\"\n",
                                template_.getName(), spec.substring(start, matcher.end()));
                    }
                    if (log_ != null)
                    {
                        log_.printf("%s biome type %s\n", (matcher.group(1) != null ? "excluding" : "including"), matcher.group(2));
                    }
                    (matcher.group(1) != null ? excluded_ : included_).add(matcher.group(2));
                    start = matcher.end();
                }
                else
                {
                    System.err.printf("cannot parse text in biome type list; template=\"%s\", text=\"%s\"\n",
                            template_.getName(), spec.substring(start));
                    break;
                }
            }
            if (included_.isEmpty() && !excluded_.isEmpty())
            {
                if (log_ != null)
                {
                    log_.printf("including biome type ALL (implicit)\n");
                }
                included_.add("ALL");
            }
        }

        // does the given set of biome type names satisfy this criterion?
        public boolean satisfiedBy(Set<String> type_names)
        {
            return Collections.disjoint(type_names, excluded_) && type_names.containsAll(included_);
        }
    }

    private List<Criterion> criteria_;

    // create a new set of criteria
    public RuinBiomeTypeCriteria(RuinTemplate template, PrintWriter log)
    {
        template_ = template;
        log_ = log;
        criteria_ = new ArrayList<>();
    }

    // parse given specification string into criterion objects
    public void addCriteria(String specs)
    {
        for (String spec : specs.toUpperCase().split(","))
        {
            criteria_.add(new Criterion(template_, log_, spec));
        }
    }

    // does the given biome satisfy all criteria?
    public boolean satisfiedBy(Biome biome)
    {
        Set<String> type_names = new HashSet<>();
        BiomeDictionary.getTypes(biome).forEach(type -> type_names.add(type.getName().toUpperCase()));
        type_names.add("ALL");
        for (Criterion criterion : criteria_)
        {
            if (criterion.satisfiedBy(type_names))
            {
                return true;
            }
        }
        return false;
    }
}
