package fr.sictiam.stela.convocationservice.service.util;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvConstraintViolationException;
import org.supercsv.util.CsvContext;

import java.util.regex.Pattern;

public class NullableRegexProcessor extends CellProcessorAdaptor implements StringCellProcessor {

    private final String regex;
    private final Pattern regexPattern;

    public NullableRegexProcessor(final String regex) {
        super();
        checkPreconditions(regex);
        regexPattern = Pattern.compile(regex);
        this.regex = regex;
    }

    public NullableRegexProcessor(final String regex, final StringCellProcessor next) {
        super(next);
        checkPreconditions(regex);
        regexPattern = Pattern.compile(regex);
        this.regex = regex;
    }

    @Override
    public Object execute(Object value, CsvContext context) {

        if (value != null && !regexPattern.matcher((String) value).matches()) {
            throw new SuperCsvConstraintViolationException(String.format(
                    "'%s' does not match the regular expression '%s'", value, regex), context, this);
        }

        return next.execute(value, context);
    }

    private static void checkPreconditions(final String regex) {
        if (regex == null) {
            throw new NullPointerException("regex should not be null");
        } else if (regex.length() == 0) {
            throw new IllegalArgumentException("regex should not be empty");
        }
    }
}
