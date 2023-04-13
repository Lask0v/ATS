package queryprocessor.preprocessor.validators;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.querytree.ConditionNode;
import queryprocessor.querytree.RelationshipRef;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Do przemyslenia ta fabryka...
 * Albo tworzyć łancuchy validatorów w niej albow konstruktorach odpowiednich klas validatorów relacji
 */

public class ValidatorFactory
{
    public static Validator createRelationshipValidator(RelationshipRef ref)
    {
        Validator validator = null;
        switch (ref.getRelationshipType())
        {
            case T_PARENT:
            case PARENT:
                validator = new AggregatedValidator(createParentValidatorChain(ref));
                break;
            case CALLS:
                validator = new AggregatedValidator(createCallsValidatorChain(ref));
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return validator;
    }

    private static List<Validator> createParentValidatorChain(RelationshipRef ref) {
        var chain = new ArrayList<Validator>();

        final var args = 2;
        chain.add(new ArgumentNumberValidator(ref, args)); // przenies informacje o ilosc arg itp do Statycznej tabeli

        chain.add(new IsContainerStatement(ref.getArg(0)));

        var arg = ref.getArg(1);
        chain.add(
                new IsNotValidator(
                        new IsProcedureValidator(arg))
        );

        return chain;
    }

     private static List<Validator> createCallsValidatorChain(RelationshipRef ref)
     {
         var chain = new ArrayList<Validator>();

         chain.add(new ArgumentNumberValidator(ref, 2)); // przenies informacje o ilosc arg itp do Statycznej tabeli

         for (int i = 0; i < ref.getArgSize(); i++ )
            chain.add(new IsProcedureValidator(ref.getArg(i)));

         return chain;
     }

     public static Validator createConditionValidator(ConditionNode node) {
         return new ConditionValidator(node);
     }
}
