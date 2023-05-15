package queryprocessor.querytree;

import pkb.ast.abstraction.ASTNode;

public class ConditionRefValue extends Condition
{
    private final AttrRef attrRef;
    private final AttrValue attrValue;

    public ConditionRefValue(AttrRef ref, AttrValue attrValue) {
        super("Attr: " + ref.getSynonym().getIdentifier()+"."+ref.getAttr().getName() + " = " + attrValue.getValue());
        this.attrRef = ref;

        ref.setParent(this);
        ref.setRightSibling(attrValue);
        attrValue.setParent(this);
        this.setFirstChild(ref);
        this.attrValue = attrValue;
    }

    @Override
    public boolean attrCompare(Object o)
    {
        Object value = attrValue.getValueType() == AttrValue.ValueType.NUMBER ? Integer.parseInt(attrValue.getValue()) : attrValue.getValue();
        return attrRef.extractAttributeValue((ASTNode) o).equals(value);
    }

    public AttrRef getAttrRef() {
        return attrRef;
    }

    public AttrValue getAttrValue() {
        return attrValue;
    }
}
