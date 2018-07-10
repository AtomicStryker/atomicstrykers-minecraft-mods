package atomicstryker.ruins.common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// utility for hiding troublesome commas to simplify rule parsing
class RuinTextLumper
{
    private RuinTemplate template_;
    private PrintWriter log_;
    private List<String> lumps_;

    // create new lumper
    public RuinTextLumper(RuinTemplate template, PrintWriter log)
    {
        template_ = template;
        log_ = log;
        lumps_ = new ArrayList<>();
    }

    private static final Pattern LUMP_PATTERN = Pattern.compile("(\\{)|\\[[^]]*+]|Lu\\d++mP");
    private static final Pattern BRACED_LUMP_PATTERN = Pattern.compile("(})|(\")|\\{");
    private static final Pattern QUOTED_LUMP_PATTERN = Pattern.compile("(\")|\\\\.");

    // replace problematic text ("lumps") with Lu#mP placeholders:
    // 1) {text in {possibly nested} braces}, such as NBT tags
    // 2) [text in brackets], such as block state properties
    // 3) unlikely text that happens to look like a placeholder
    // note: all previous placeholder data is discarded
    public String lump(String input)
    {
        lumps_.clear();
        StringBuilder output = new StringBuilder();
        int start = 0;
        final int end = input.length();
        Matcher matcher = LUMP_PATTERN.matcher(input);
        while (start < end)
        {
            if (matcher.find(start))
            {
                int extent = matcher.end();
                if (matcher.group(1) != null)
                {
                    extent = extendNestedLump(BRACED_LUMP_PATTERN, input, extent, end);
                }
                if (extent >= start)
                {
                    output.append(input.substring(start, matcher.start())).append("Lu").append(lumps_.size()).append("mP");
                    String lump = input.substring(matcher.start(), extent);
                    lumps_.add(lump);
                    if (log_ != null)
                    {
                        log_.println("template " + template_.getName() + " contains lump: " + lump);
                    }
                    start = extent;
                }
                else
                {
                    System.err.println("Unbalanced nesting in Ruins template " + template_.getName() + ", offending rule: " + input);
                    output.append(input.substring(start));
                    break;
                }
            }
            else
            {
                output.append(input.substring(start));
                break;
            }
        }
        return output.toString();
    }

    // find end position of entire (possibly) nested lump
    private int extendNestedLump(Pattern pattern, String input, int start, int end)
    {
        int depth = 0;
        Matcher matcher = pattern.matcher(input);
        while (start < end)
        {
            if (matcher.find(start))
            {
                start = matcher.end();
                if (matcher.group(1) != null)
                {
                    if (--depth < 0)
                    {
                        return start;
                    }
                }
                else if (matcher.group(2) != null)
                {
                    if ((start = extendQuotedLump(input, start, end)) < 0)
                    {
                        break;
                    }
                }
                else
                {
                    ++depth;
                }
            }
            else
            {
                break;
            }
        }
        return -1;
    }

    // find end position of entire quoted lump
    private int extendQuotedLump(String input, int start, int end)
    {
        Matcher matcher = QUOTED_LUMP_PATTERN.matcher(input);
        while (start < end)
        {
            if (matcher.find(start))
            {
                start = matcher.end();
                if (matcher.group(1) != null)
                {
                    return start;
                }
            }
            else
            {
                break;
            }
        }
        return -1;
    }

    private static final Pattern UNLUMP_PATTERN = Pattern.compile("Lu(\\d++)mP");

    // replace lump placeholders with original text
    public String unlump(String input)
    {
        StringBuilder output = new StringBuilder();
        int start = 0;
        final int end = input.length();
        Matcher matcher = UNLUMP_PATTERN.matcher(input);
        while (start < end)
        {
            if (matcher.find())
            {
                output.append(input.substring(start, matcher.start())).append(lumps_.get(Integer.parseInt(matcher.group(1))));
                start = matcher.end();
            }
            else
            {
                output.append(input.substring(start));
                break;
            }
        }
        return output.toString();
    }
}
