package queryprocessor.preprocessor.validators;

import java.util.List;

public class AggregatedValidator implements Validator
{
  //private final RelationshipRef rel;
  private final List<Validator> validatorsChain;
  private String lastErrorMsg;

  public AggregatedValidator(List<Validator> validatorsChain) {
    this.validatorsChain = validatorsChain;
  }

  @Override
  public boolean isValid()
  {
    for (Validator validator: validatorsChain) {
      if (!validator.isValid()) {
        lastErrorMsg = validator.getErrorMsg();
        return false;
      }
    }

    return true;
  }

  @Override
  public String getErrorMsg() {
    return lastErrorMsg;
  }
}
