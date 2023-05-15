package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.ArgNode;

import java.util.ArrayList;
import java.util.List;

public class IsContainerStatement implements Validator
{
    private final IsAnyValidator isAnyValidator;

    public IsContainerStatement(ArgNode arg) {
        String containerType = "Container-type Statement";
        ArrayList<Validator> list = new ArrayList<Validator>(){{
            add(new ArgTypeValidator(arg, Keyword.STATEMENT, containerType));
            add(new ArgTypeValidator(arg, Keyword.WHILE, containerType));
            add(new ArgTypeValidator(arg, Keyword.IF, containerType));
            add(new ArgTypeValidator(arg, Keyword.PROCEDURE, containerType));
        }};
        isAnyValidator = new IsAnyValidator(list);
    }

    @Override
    public boolean isValid() {
        return isAnyValidator.isValid();
    }

    @Override
    public String getErrorMsg() {
        return isAnyValidator.getErrorMsg();
    }
}
