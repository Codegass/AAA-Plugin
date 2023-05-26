package plugin1.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;


public class GenericMethodVisitor extends ASTVisitor{
    MethodDeclaration method = null;
    private IMethodBinding binding;
    private ITypeBinding[] itbs; 
    
    public GenericMethodVisitor(IMethodBinding binding) {
    	this.binding = binding;
    	itbs = binding.getParameterTypes();
    	//System.out.println(binding.getName());
		/*
		 * for (ITypeBinding itb : itbs) { System.out.println("binding itb name: " +
		 * itb.getBinaryName()); System.out.println("binding generic?: " +
		 * itb.isGenericType()); }
		 */
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (match(node)) method = node;
        return super.visit(node);
    }

    public MethodDeclaration getMethod() {
        return method;
    }
    
	/*
	 * binding may be a copy of a generic method with substitutions for the method's
	 * type parameters
	 * 
	 */    
    private boolean match(MethodDeclaration md) {
    	String name = md.getName().toString();
    	//System.out.println(name);
    	if (!name.equals(binding.getName())) return false;
    	//System.err.println(md);
    	//for (ITypeBinding itb: binding.)
    	List actualParameters = md.parameters();
    	ITypeBinding[] itbs2 = new ITypeBinding[actualParameters.size()];
    	for (int i = 0; i < itbs2.length; i++) {
    		itbs2[i] = ((SingleVariableDeclaration)actualParameters.get(i)).getType().resolveBinding();
    	}
    	if (!allParametersMatch(itbs, itbs2)) return false;
    	
/*    	for (int i = 0; i < actualParameters.size(); i++) {
    		SingleVariableDeclaration svd = (SingleVariableDeclaration)actualParameters.get(i);
    		System.out.println("svd type resolve: " + svd.getType());
    		System.out.println("svd type resolve: " + svd.getType().resolveBinding().getQualifiedName());
    		System.out.println("svd type resolve: " + svd.getType().resolveBinding().isParameterizedType());
    		ITypeBinding[] itbs = svd.getType().resolveBinding().getTypeArguments();
    		System.out.println(itbs.length);
    		for (ITypeBinding itb : itbs) {
    			System.out.println(itb);
    			System.out.println(itb.getKind());
    			System.out.println(itb.isWildcardType());
    			System.out.println(itb.isRawType());
    			System.out.println(itb.isGenericType());
    			System.out.println(itb.isParameterizedType());
    			System.out.println(itb.isCapture());
    			System.out.println(itb.isClass());
    			System.out.println(itb.isUpperbound());
    			System.out.println(itb.isTypeVariable());
    			System.out.println(itb.getModifiers());
    			if (itb.getTypeBounds().length != 0) System.out.println(itb.getTypeBounds()[0]);
    		}
    		//System.out.println("svd resolvebinding keys " + svd.resolveBinding().getKey());
    		//System.out.println("svd resolvebinding generic? " + svd.resolveBinding().getType().isGenericType());
    		//System.out.println("svd resolvebinding getType: " + svd.resolveBinding().getType());
    	}*/
    	//System.out.println();
    	//System.out.println("Found: " + md.getName());
    	return true;
    }
    
    private boolean allParametersMatch(ITypeBinding[] itbs1, ITypeBinding[] itbs2) {
    	if (itbs1.length != itbs2.length) return false;
    	
    	for (int i = 0; i < itbs1.length; i++) {
    		if (!singleParameterMatch(itbs1[i], itbs2[i])) return false;
    	}
    	
    	return true;
    }
    
    private boolean singleParameterMatch(ITypeBinding itb1, ITypeBinding itb2) {
    	if (itb2.getModifiers() == 0) return true; // itb2 == ?, T 
    	// ? T extends org.xxx.xxx not checked
    	// To do:
    	
    	if (itb1.isEqualTo(itb2)) return true;
    	if (!itb1.getBinaryName().equals(itb2.getBinaryName())) {
    		//System.out.println("binary name not equal");
    		//System.out.println(itb1.getBinaryName());
    		//System.out.println(itb2.getBinaryName());
    		return false;
    	}
    	
    	if (!allParametersMatch(itb1.getTypeArguments(), itb2.getTypeArguments())) return false;
    	
    	return true;
    }
}
