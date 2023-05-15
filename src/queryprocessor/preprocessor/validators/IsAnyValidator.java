package queryprocessor.preprocessor.validators;

import java.util.List;

/**
 * Logiczny OR Validator
 */

public class IsAnyValidator implements Validator
{
    private final List<Validator> validatorsChain;
    private String lastErrorMsg;

    public IsAnyValidator(List<Validator> validators) {
        this.validatorsChain = validators;
    }

    @Override
    public boolean isValid() {
        for (Validator validator: validatorsChain)
            if(validator.isValid())
                return true;
            else
                lastErrorMsg = validator.getErrorMsg();

        return false;
    }

    @Override
    public String getErrorMsg() {
        return lastErrorMsg;
    }
}
