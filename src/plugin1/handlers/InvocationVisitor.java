package plugin1.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

public class InvocationVisitor extends ASTVisitor{
    List<ASTNode> methods = new ArrayList<>();
    List<ASTNode> order = new ArrayList<>();
//    LinkedHashMap<MethodInvocation, Integer> pending = new LinkedHashMap<>();
//
//    Map<MethodInvocation, List<MethodInvocation>> parameters = new HashMap<>();
//    Map<MethodInvocation, MethodInvocation> chain = new HashMap<>();

//    List<MethodInvocation> pending = new ArrayList<>();
	
    @Override
    public boolean visit(MethodInvocation node) {    	
    	methods.add(node);
        
        if (!order.contains(node)) {
        	order.addAll(getPotential(node));
        }
       
        
//    	pending.add(node);
        
//        if (!potentialChains.contains(removeChain(node.toString()))) {
//        	// not in chain
//        	refreshPotentialChains(node.toString());
//        	for (int i = pending.size() - 1; i >= 0; i--) {
//        		System.out.println("add pending: " + pending.get(i));
//        		methods.add(pending.get(i));
//        	}
//        	pending.clear();
//        }
//        
//        pending.add(node);
//        
//        if (potentialParameters.contains(node.toString())) {
//            System.out.println(">> in potential ");
//        } else {
//        	// if in chain
//        	if (!potentialChains.contains(removeChain(node.toString()))) {
//        		potentialParameters.clear();
//        	}
//        	refreshPotentialParameters(node.toString());
//        	potentialParameters.forEach((argument) -> {
//        		System.out.println(">> Parameters: " + argument);
//        	});
//        }
        return true;
    }
    
//    private void constructMap(MethodInvocation mi) {
//    	List<MethodInvocation> invocations = new ArrayList<>();
//    	
//    	Expression caller = mi.getExpression();
//        if (caller != null && caller.getNodeType() == ASTNode.METHOD_INVOCATION) {
////        	invocations.add((MethodInvocation) caller);
//        	chain.put(mi, (MethodInvocation) caller);
////        	constructMap((MethodInvocation) caller);
//        }
//            
//        List<ASTNode> arguments = mi.arguments();
//        for (ASTNode arg : arguments) {
//        	if (arg.getNodeType() == ASTNode.METHOD_INVOCATION) {
////            	invocations.add((MethodInvocation) arg);
//        		if (parameters.get(mi) == null) {
//        			parameters.put(mi, new ArrayList<>());
//        		}
//        		parameters.get(mi).add((MethodInvocation) arg);
//        		constructMap((MethodInvocation) arg);
//        	}
//        }
//   	}
//    
    private List<ASTNode> getPotential(ASTNode node) {
    	List<ASTNode> invocations = new ArrayList<>();
    	Expression caller = null;
    	List<ASTNode> arguments = null;
    	if (node instanceof MethodInvocation) {
    		MethodInvocation mi = (MethodInvocation) node;
    		caller = mi.getExpression();
    		arguments = mi.arguments();
    	} else if (node instanceof ClassInstanceCreation) {
    		ClassInstanceCreation cic = (ClassInstanceCreation) node;
        	caller = cic.getExpression();
        	arguments = cic.arguments();
    	} else if (node instanceof ExpressionMethodReference) {
    		ExpressionMethodReference emr = (ExpressionMethodReference) node;
    		caller = emr.getExpression();
    	}
    	
        if (caller != null && (
        		caller.getNodeType() == ASTNode.METHOD_INVOCATION || 
        		caller.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION || 
        		caller.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE
    		)) {
        	invocations.addAll(getPotential(caller));
        }
        
        if (arguments != null) {
        	for (ASTNode arg : arguments) {
            	if (arg.getNodeType() == ASTNode.METHOD_INVOCATION || 
        			arg.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION || 
        			arg.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE
        		) {
            		invocations.addAll(getPotential(arg));
            	}
            }
        }
        
        invocations.add(node);
        
        return invocations;
	}
//    
    private void sortMethods() {
    	Collections.sort(methods, Comparator.comparing(n -> order.indexOf(n)));
	}
//    
//    private List<ASTNode> getOrderedOfCurrent(ASTNode node) {
//    	List<ASTNode> tmp = new ArrayList<>();
//    	List<ASTNode> chainList = new ArrayList<>();
//
//    	tmp.add(node);
//    	
//    	ASTNode caller = chain.get(node);
//		while (caller != null) {
//			if (methods.contains(caller)) {
//				tmp.add(0, caller);
//			}
//			caller = chain.get(caller);
//		}
//		
//		chainList = List.copyOf(tmp);
//		
//		for (ASTNode n: chainList) {
//			List<MethodInvocation> args = parameters.get(n);
//			if (args == null) continue;
//			for (MethodInvocation arg : args) {
//				if (arg != null && methods.contains(arg)) {
//					tmp.add(tmp.indexOf(n), arg);
//					tmp.addAll(tmp.indexOf(arg), getOrderedOfCurrent(arg));
//				}
//			}
//		}
//		
//		return tmp;
//    }
	
	@Override
    public boolean visit(ExpressionMethodReference node) {
    	methods.add(node);
    	
    	if (!order.contains(node)) {
        	order.addAll(getPotential(node));
        }
    	
    	return true;
    }
    
    @Override
    public boolean visit(ClassInstanceCreation node) {
        methods.add(node);
        
        if (!order.contains(node)) {
        	order.addAll(getPotential(node));
        }
        
        return true;
    }
    
    @Override
    public boolean visit(NormalAnnotation node) {    	
    	// if format @Test(expected=..)
    	if (node.getTypeName().toString().equals("Test")) {
        	List<MemberValuePair> values = node.values();
        	for (MemberValuePair v : values) {
        		if (v.getName().toString().equals("expected")) {
        			methods.add(node);
        		}
        	}
    	}

        return true;
    }
    
    public List<ASTNode> getMethods() {
    	sortMethods();
        return methods;
    }
    

    
    
//    public void refreshPotentialParameters(String nodeStr) {
////    	String nodeStr = node.toString();
//    	nodeStr = removeChain(nodeStr);
//    	System.out.println("debug >>> " + nodeStr);
//
//    	if (nodeStr.isEmpty()) {
//    		return;
//    	}
//    	
//    	String parametersStr = nodeStr.substring(nodeStr.indexOf("(") + 1, nodeStr.lastIndexOf(")"));
//		if (parametersStr.isEmpty()) {
//			return;
//		}
//    	String[] parameters = parametersStr.split(",");
//    	for (String p : parameters) {
//    		if (p.contains("(")) {
//    			potentialParameters.add(p);
//    			refreshPotentialParameters(p);
//    		}
//    	}
//    }
    
    
  
    
    
    
    
    
    
    
    
//    public String removeChain(String nodeStr) {
//    	int checker = 0;
//    	int indexToSplit = 0;
//    	
//    	for (int i = 0; i < nodeStr.length(); i++) {
//    		Character c = nodeStr.charAt(i);
//    		
//    		if (c == '(') {
//    			checker++;
//    		}
//    		if (c == ')') {
//    			checker--;
//    		}
//    		if (c == '.' && checker == 0) {
//    			indexToSplit = i;
//    		}
//    	}
//    	
//    	if (indexToSplit == 0) {
//    		return nodeStr;
//    	}
//    	
//    	return nodeStr.substring(indexToSplit + 1);
//    }
//    
//    public boolean isFirstLevelContainsDot(String str) {
//    	int checker = 0;
//    	
//    	for (int i = 0; i < str.length(); i++) {
//    		Character c = str.charAt(i);
//    		
//    		if (c == '(') {
//    			checker++;
//    		}
//    		if (c == ')') {
//    			checker--;
//    		}
//    		if (c == '.' && checker == 0) {
//    			return true;
//    		}
//    	}
//    	
//    	return false;
//    }
//    
//    public List<String> getPotentialInvocations(String nodeStr) {
//    	int checker = 0;
//    	int doubleQuoteChecker = 1;
//    	List<String> potential = new ArrayList<>();
//    	List<String> potentialChains = new ArrayList<>();
//    	
//    	if (!nodeStr.contains("(")) {
//    		return potential;
//    	}
//    	
//    	// construct potential chains
//    	for (int i = 0; i < nodeStr.length(); i++) {
//    		Character c = nodeStr.charAt(i);
//    		
//    		if (c == '"') {
//    			doubleQuoteChecker *= -1;
//    		}
//    		if (c == '(' || c == '{') {
//    			checker++;
//    		}
//    		if (c == ')' || c == '}') {
//    			checker--;
//    		}
//    		if (c == '.' && checker == 0 && doubleQuoteChecker == 1) {
//    			String currentPart = nodeStr.substring(0, i);
//    			if (currentPart.contains("(")) {
//    				potentialChains.add(currentPart);
//    			}
//    		}
//    	}
//    	
//    	potentialChains.add(nodeStr);
//    	
//    
//    	// for each potential chains
//    	for (String partOfChain : potentialChains) {
//    		if (partOfChain.contains("(")) {
//    			potential.addAll(getPotentialParameters(partOfChain));
//    		}
//        	potential.add(partOfChain);
//    	}
//    	
//    	return potential;
//    }
//    
//    
//    public List<String> getPotentialParameters(String nodeStr) {
////    	String nodeStr = node.toString();
//    	List<String> potentialParameters = new ArrayList<String>();
//    	nodeStr = removeChain(nodeStr);
//    	
//    	if (nodeStr.isEmpty()) {
//    		return potentialParameters;
//    	}
//    	    	
//    	String parametersStr = nodeStr.substring(nodeStr.indexOf("(") + 1, nodeStr.lastIndexOf(")"));
//    	System.out.println("parametersStr >> " + parametersStr);
//
//    	if (parametersStr.isEmpty()) {
//			return potentialParameters;
//		}
//		
//    	List<String> parameters = splitFirstLevelParameters(parametersStr);
//    	for (String p : parameters) {
//        	System.out.println("parameters >> " + p);
////    		if (isFirstLevelContainsDot(p)) {
////    			potentialParameters.addAll(getPotentialInvocations(p));
////    		} else if (p.contains("(")) {
////    			potentialParameters.add(p);
////    			potentialParameters.addAll(getPotentialParameters(p));
////    		}
//    	}
//    	
//    	return potentialParameters;
//    }
//    
//    public List<String> splitFirstLevelParameters(String str) {
//      	int previous = 0;
//      	int checker = 0;
//      	int doubleQuoteChecker = 1;
//      	List<String> parameters = new ArrayList<String>();
//    	// construct potential chains
//    	for (int i = 0; i < str.length(); i++) {
//    		Character c = str.charAt(i);
//    		if (c == '"') {
//    			doubleQuoteChecker *= -1;
//    		}
//    		if (c == '(' || c == '{') {
//    			checker++;
//    		}
//    		if (c == ')' || c == '}') {
//    			checker--;
//    		}
//    		if (c == ',' && checker == 0 && doubleQuoteChecker == 1) {
//    			parameters.add(str.substring(previous, i));
//    			previous = i + 1;
//    		}
//    	}
//    	
//    	parameters.add(str.substring(previous));
//    	
//    	return parameters;
//    }
    
//    public void refreshPotentialInvocation(String nodeStr) {
//    	// chain level
//    	int checker = 0;
//    	int previousSplitIndex = 0;
//    	
//    	for (int i = 0; i < nodeStr.length(); i++) {
//    		Character c = nodeStr.charAt(i);
//    		
//    		if (c == '(') {
//    			checker++;
//    		}
//    		if (c == ')') {
//    			checker--;
//    		}
//    		if (c == '.' && checker == 0) {
//    			String currentPart = nodeStr.substring(previousSplitIndex + 1, i);
//    			if (currentPart.contains("(")) {
//    				potentialChains.add(currentPart);
//    			}
//    			previousSplitIndex = i;
//    		}
//    	}
//    	
//    	
////    	String nodeStr = node.toString();
//    	nodeStr = removeChain(nodeStr);
//    	System.out.println("debug >>> " + nodeStr);
//
//    	if (nodeStr.isEmpty()) {
//    		return;
//    	}
//    	
//    	String parametersStr = nodeStr.substring(nodeStr.indexOf("(") + 1, nodeStr.lastIndexOf(")"));
//		if (parametersStr.isEmpty()) {
//			return;
//		}
//    	String[] parameters = parametersStr.split(",");
//    	for (String p : parameters) {
//    		if (p.contains("(")) {
//    			potentialParameters.add(p);
//    			refreshPotentialParameters(p);
//    		}
//    	}
//    }

}
