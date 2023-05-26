package plugin1.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ParameterVisitor extends ASTVisitor{
    List<ASTNode> methods = new ArrayList<>();
    
    @Override
    public boolean visit(MethodInvocation node) {
        methods.add(node);
       
        return true;
    }
    
    
    public List<ASTNode> getMethods() {
//    	flushPending();
        return methods;
    }
   
}
