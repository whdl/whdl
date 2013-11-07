package whdl.frontend.ast;

public class AnonymousFunctionExpression extends Expression {
	private Identifier inputID;
	private Typename inputTypename;
	private Identifier outputID;
	private Typename outputTypename;
	private StatementList statements;
	
	public AnonymousFunctionExpression(Identifier inputID, Typename inputTypename, 
			Identifier outputID, Typename outputTypename, StatementList statements){		
		this.inputID = inputID;
		this.inputTypename = inputTypename;
		this.outputID = outputID;
		this.outputTypename = outputTypename;
		this.statements = statements;
	}
		
	public Identifier getInputID(){return inputID;}
	public Typename getInputTypename(){return inputTypename;}
	public Identifier getOutputID(){return outputID;}
	public Typename getOutputTypename(){return outputTypename;}
	public StatementList getStatements(){return statements;}
}