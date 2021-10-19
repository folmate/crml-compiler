package crml.translator;

import java.util.HashMap;
import java.util.List;

import crml.crmlBaseVisitor;
import crml.crmlParser;
import crml.crmlParser.DefinitionContext;
import crml.crmlParser.Element_defContext;
import crml.crmlParser.IdContext;
import crml.crmlParser.User_keywordContext;

public class crmlVisitorImpl extends crmlBaseVisitor<Value> {
		
		private Integer counter;

		//type mapping
		private HashMap<String, String> types_mapping;
		
		//operator mapping
	    private HashMap<String, String> operators_mapping;
		
		
		//types of variables for operator typing
		private HashMap<String, String> variable_types;
		
		private StringBuffer blockDefinitions;
		
		public crmlVisitorImpl () {		
			
			types_mapping = new HashMap<String, String>();
			
			types_mapping.put("Boolean", "CRML.Types.Boolean4");
			types_mapping.put("Period",  "CRML.TemporalBlocks.Period");
			
			operators_mapping = new HashMap<String, String>();
			operators_mapping.put("binbool+", 	"CRML.Operators.add4");
			operators_mapping.put("binbool-", 	"CRML.Operators.sub4");
			operators_mapping.put("binbool*", 	"CRML.Operators.mul4");
			operators_mapping.put("binbooland", "CRML.Operators.and4");
			
			variable_types = new HashMap<String, String>();	
			
			blockDefinitions = new StringBuffer();
			
			counter = 0; //used to create unique names for automatically generated blocks
			
		}		
		
		@Override public Value visitDefinition(DefinitionContext ctx) {
			StringBuffer buffer = new StringBuffer();
			
				//TODO support for library and package
				buffer.append("model " + ctx.id(0).getText() + " \n");
				
				List<Element_defContext> cL = ctx.element_def();
				
				for (Element_defContext e : cL)
					buffer.append(visit(e).contents);
				
				buffer.append(blockDefinitions);
					
				buffer.append("end " + ctx.id(1).getText()+ ";\n"); 
				
				
				return new Value (buffer.toString(), "Program");
			}
		
		@Override public Value visitElement_def(crmlParser.Element_defContext ctx) {
			
			// the element is a definition
			if(ctx.var_def() != null)
				return visit(ctx.var_def());
			
			// the element is a template
			if (ctx.template() != null)
				return visit (ctx.template());
			
			// the element is a 
			
			return new Value ("Unknown "+ctx.getText()+ '\n', "Element");
		}
		
		@Override public Value visitTemplate(crmlParser.TemplateContext ctx) {
			
			StringBuffer definition = new StringBuffer("model ");
			StringBuffer modelName = new StringBuffer();
			
			HashMap<String, String> store_variable_types = (HashMap<String, String>) variable_types.clone();
			
			// generate function name
			for (User_keywordContext k : ctx.user_keyword()) {
				modelName.append(k.getText());
			}
			
			definition.append(modelName);
			definition.append("\n");
			
			String bType = types_mapping.get("Boolean");
			
			// generate variables
			for (IdContext v : ctx.id()) {
				definition.append("input " + bType + " " + v.getText() + ";\n");
				variable_types.put(v.getText(), "Boolean");
			}
			
			definition.append("output " + bType + " out; \n");
			
			// append body
			Value exp = visit(ctx.exp());
			definition.append("equation \n out =" + exp.contents + ";\n");
			
			definition.append("end ");
			definition.append(modelName);
			definition.append(";\n");
			
			// get rid of all local variables
			variable_types = store_variable_types;
			
			return new Value (definition.toString(), "Template");
		}
		
		
		@Override public Value visitVar_def(crmlParser.Var_defContext ctx) {
			String var_t, mapped_t;
			StringBuffer buffer = new StringBuffer();
			
			
				
				var_t = ctx.type().getText();
				if(types_mapping.containsKey(var_t))
					mapped_t= types_mapping.get(var_t);
				else
					mapped_t=var_t;
				
				buffer.append(mapped_t + " ");		
				buffer.append(ctx.id().getText());	
				
				variable_types.put(ctx.id().getText(), var_t);
				
				if(ctx.exp()!=null) {
					buffer.append(" = " + visit(ctx.exp()).contents);	
				}
				buffer.append(";\n");	
			
			return new Value(buffer.toString(), "Definition");
		}
		
		@Override public Value visitExp(crmlParser.ExpContext ctx) {
			Value right, left, exp;
			
			// if it the expression is a constant			
			if(ctx.constant()!=null)
				return this.visit(ctx.constant());
			
			// if the expression is a variable
			if(ctx.id()!=null) {
				if (variable_types.containsKey(ctx.getText()))
					 return new Value (ctx.getText(), variable_types.get(ctx.getText()));	
				
				else return new Value ("not found", "oops");
			} 
							
			// if the expression is a binary operator
			if(ctx.binary_op()!=null) {
				left = visit(ctx.exp(0));
				right = visit(ctx.exp(1));
				
				Boolean isUserOp = !(ctx.binary_op().user_keyword()==null);
				Value result = apply_binary_op(left, right, ctx.binary_op().getText(), isUserOp);
				return result;				
			}
			
			// if the expression is in paranthesis
			if(ctx.sub_exp()!=null)
				return visit(ctx.sub_exp().exp());
				
				
			// if the expression is a right unary operator
			
			// if the expression is a right unary operator
			if(ctx.right_unary_op()!=null) {
				right = visit(ctx.exp(0));
				Value result = apply_unary_op(ctx.right_unary_op().getText(), right);
				return result;
			}
			
			
			return null;
		}

		private Value apply_binary_op(Value left, Value right, String op, Boolean isUserOp) {
			switch (left.type) {
			case "Boolean":
				return new Value (applyBooleanOp(left, op, right, isUserOp), "Boolean");
			case "Number":
				if (isUserOp) {
					String name=op+counter;
					String res = op+ " " + name+ "(r1="+left+",r2="+right+");";
					blockDefinitions.append(res);
					String res2 = op+ " " + name+ "(r1="+left+",r2="+right+");";
					counter++;
					return new Value ("name.out", "Number");}
				return new Value (left.contents + " " + op + " " + right.contents, "Number");
			default:
				break;
			}
			return new Value (op + " notParsed", "unknown");
		}
		
		private Value apply_unary_op(String op, Value right) {
			switch (right.type) {
			case "Boolean":
				return new Value (applyBooleanOp(op, right), "Boolean");
			case "Number":
				return new Value (op + " " + right.contents, "Number");
			default:
				break;
			}
			return new Value (op + " notParsed", "unknown");
		}

		private String applyBooleanOp(Value left, String op, Value right, Boolean isUserOp ) {
			
				if (isUserOp) {
					String name=op+counter;
					String res = op+ " " + name+ "(r1="+left.contents+",r2="+right.contents+");";
					blockDefinitions.append(res);
					counter++;
					return name+".out";}
			if(operators_mapping.containsKey("binbool"+op))
				return operators_mapping.get("binbool"+op) + "("+ left.contents + "," + right.contents + ")";
			
			return "unapplicable operator" + op;
		}
		
		private String applyBooleanOp(String op, Value right) {
			
			switch (op) {
			case "not":
				return ("CRML.Operators.not4("+ right.contents +")");
			case "pre": // pre is time dependent, need to generate a block
				String bl = "pre"+counter++;
				blockDefinitions.append("CRML.Operators.PreBool4 " + bl + "(" + right.contents + ");\n");
				return (bl + ".out");
			default:
				break;
			}
			return "unapplicable operator" + op;
		}
		

		@Override public Value visitConstant(crmlParser.ConstantContext ctx) {
			
			if(ctx.boolean_value()!=null)
				return visit(ctx.boolean_value());
			
			if(ctx.string()!=null)
				return new Value(ctx.string().getText(), "String");
			
			if(ctx.number()!=null)
				return new Value(ctx.number().getText(), "Number");
			
			String val = ctx.getText();		
			
			return new Value("Unknown Type " + val, "Unknown");
		}
		
		@Override public Value visitBoolean_value(crmlParser.Boolean_valueContext ctx) {
			String bool_val, translated_val;
			
			bool_val = ctx.getText();	
			
			switch(bool_val) {
			  case "true":			return new Value ("CRML.Types.Boolean4.true4", "Boolean");
			  case "false":			return new Value ("CRML.Types.Boolean4.false4", "Boolean");
			  case "undecided": 	return new Value ("CRML.Types.Boolean4.undecided", "Boolean");
			  case "undefined":		return new Value ("CRML.Types.Boolean4.undefined", "Boolean");
			  default:				return new Value ("error", "Error");
			}
			
		}

}
