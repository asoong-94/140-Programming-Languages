/* *** This file is given as part of the programming assignment. *** */
import java.util.*;
import java.lang.*;

public class Parser {
    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	    tok = scanner.scan();
    }
    private SymbolTable table;
    
    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	table = new SymbolTable();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }


     // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }
    
    
    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
    
    private void program() {
        System.out.println("#include <stdio.h>");
        System.out.println("int main() { ");
    	block();
    	System.out.println("return 0;");
    	System.out.println("}");
    }

    private void block(){
        table.newscope();
    	declaration_list();
    	statement_list();
    	table.deletescope();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
    	while( is(TK.DECLARE) ) {
    	    declaration();
    	    System.out.print(";");
    	}
    }

    private void declaration() {
    	mustbe(TK.DECLARE);
    	if(is(TK.ID))
    	{
    	    if(!table.contains(tok.string))
    	    {
                System.out.println("int x_" + table.getscope() + tok.string);
    	        table.add(tok.string);
    	    } // what does x declare to? 
        	else
        	{
        	    while (table.contains(tok.string))
            	{
            	    //print error for redeclaration
                    System.err.println("redeclaration of variable " + tok.string);
                    scan();
                    if (is(TK.COMMA))
                        scan();
                    else
                        return;
            	}
            	//print var with scope 
            	System.out.println("int x_" + table.getscope() + tok.string);
    	        table.add(tok.string);
        	}
        	scan();
        }
        else
            mustbe(TK.ID);
            
    	while( is(TK.COMMA) ) {
    	    scan();
        	if(is(TK.ID))
    	    {
    	        if(!(table.contains(tok.string)))
    	        {
            	    System.out.print(", x_" + table.getscope() + tok.string);
            	    table.add(tok.string);
    	        }
                else
                    System.err.println("redeclaration of variable " + tok.string);
                scan();
            }
            else
                mustbe(TK.ID);
    	}
    }

    private void statement_list()
    {
        while (is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF) || is(TK.FOR))
        {
            
            if (is(TK.TILDE) || is(TK.ID) || is(TK.PRINT))
            {
                statement();
    	        System.out.print(";");
    	        System.out.println("");
            }
            else
                statement();
        }
    }

    private void statement()
    {
        if (is(TK.TILDE) || is(TK.ID))
            assignment();
        else if (is(TK.PRINT))
            print();
        else if (is(TK.DO))
            do_this();
        else if (is(TK.IF))
            if_this();
        else if (is(TK.FOR))
            forloop();
    }
    
    private void print()
    {
        mustbe(TK.PRINT);
        System.out.println("printf(\"%d\\n\", ");
        expr();
        System.out.print(")");
    }
    
    
    private void assignment()
    {
        ref_id();
        mustbe(TK.ASSIGN); 
        System.out.print(" = ");
        expr();
    }
    
    private void ref_id()
    {
        int scope;
        if (is(TK.TILDE)) //if there is a tilde
        {
            scan(); //must be tilde 
            if (is(TK.NUM))
            {
                scope = Integer.parseInt(tok.string);
                scan();
                if (is(TK.ID))
                {
                    if(!table.containsinscope(scope, tok.string))
                    {
                        System.err.println("no such variable ~" + scope + tok.string + " on line " + tok.lineNumber);
                        System.exit(1);
                    } //inner if
                    int temp = table.getscope() - scope;
                    System.out.print("x_" + (table.getscope() - scope) + tok.string);
                    scan();
                } //middle if 
                else //else to middle if 
                {
                    mustbe(TK.ID);
                }
            } //outer if, could or could not have number 
            else if (is(TK.ID))
            {
                if(!table.containsinglobal(tok.string))
                {
                    System.err.println("no such variable ~" + tok.string + " on line " + tok.lineNumber);
                    System.exit(1);
                }
                System.out.print("x_0" + tok.string);
                scan();
            }
        }
        else if (is(TK.ID))
        {
            if(!table.containsinprogram(tok.string))
            {
                System.err.println(tok.string + " is an undeclared variable on line " + tok.lineNumber);
                System.exit(1);
            }
            int temp = 0;
            while(!table.containsinscope(temp,tok.string))
                temp++;
            System.out.print("x_" + (table.getscope() - temp) + tok.string);
            scan();
        }
        else
            mustbe(TK.ID);
    }
    
    private void do_this ()
    {
        mustbe(TK.DO);
        System.out.print("while( 0 >= (");
        guarded_command();
        mustbe(TK.ENDDO);
    }
    
    //not sure
    private void if_this ()
    {
        if (is(TK.IF))
        {
            System.out.print("if ( 0 >= (");
            scan();
            guarded_command();
            while (is(TK.ELSEIF))
            {
                mustbe(TK.ELSEIF);
                System.out.println("else if ( 0 >= (");
                guarded_command();
            }
        }
        if(is(TK.ELSE))
        {
            System.out.print("else {");
            mustbe(TK.ELSE);
            block();
            System.out.print("}");
        }
        mustbe(TK.ENDIF);
    }
    
    private void guarded_command()
    {
        expr();
        System.out.println("))");
        mustbe(TK.THEN);
        System.out.println("{");
        block();
        System.out.println("}");
    }
    
    private void expr()
    {
        term();
        while ( is(TK.PLUS) || is(TK.MINUS))
        {
            addop();
            term();
        }
    }
    
    private void term()
    {
        factor();
        while (is(TK.TIMES) || is(TK.DIVIDE))
        {
            multop();
            factor();
        }
    }
    
    //DO WE NEED THE ( AND )
    private void factor()
    {
        if (is(TK.LPAREN))
        {
            System.out.print("(");
            scan();
            expr();
            mustbe(TK.RPAREN);
            System.out.print(")");
        }
         
        else if(is(TK.TILDE) || is(TK.ID))
            ref_id();
        else if(is(TK.NUM))
        {
            System.out.print(tok.string);
            scan();
        }
    }    
    
    private void addop()
    {
        if ((is(TK.PLUS) || is(TK.MINUS)))
        {
            System.out.print(" " + tok.string + " "); //addop or multop are never the end of the line
            scan();
        }
        else
        {
            System.err.println( "mustbe: want + or -, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        }
    }    
    
    private void multop()
    {
        if (is(TK.TIMES) || is(TK.DIVIDE))
        {
            System.out.print(" " + tok.string + " "); //addop or multop are never the end of the line
            scan();
        }
        else
        {
            System.err.println( "mustbe: want * or /, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        }
    }
    
    private void forloop()
    {
        mustbe (TK.FOR);
        System.out.println("for(");
        int scope;
        String var_name = "";
        if (is(TK.TILDE)) //if there is a tilde
        {
            scan(); //must be tilde 
            if (is(TK.NUM))
            {
                scope = Integer.parseInt(tok.string);
                scan();
                if (is(TK.ID))
                {
                    if(!table.containsinscope(scope, tok.string))
                    {   //if variable not declared in current scope 
                        System.err.println("no such variable ~" + scope + tok.string + " on line " + tok.lineNumber);
                        System.exit(1);
                    } //inner if
                    int temp = table.getscope() - scope;
                    //assignment 
                    var_name = "x_"+(table.getscope() - scope) + tok.string;
                    System.out.print(var_name);
                    scan();
                } //middle if 
                else //else to middle if 
                {
                    mustbe(TK.ID);
                }
            } //outer if, could or could not have number 
            else if (is(TK.ID))
            {
                if(!table.containsinglobal(tok.string))
                {
                    System.err.println("no such variable ~" + tok.string + " on line " + tok.lineNumber);
                    System.exit(1);
                }
                var_name = "x_0" + tok.string;
                System.out.print(var_name);
                scan();
            }
        }
        else if (is(TK.ID))
        {
            if(!table.containsinprogram(tok.string))
            {
                System.err.println(tok.string + " is an undeclared variable on line " + tok.lineNumber);
                System.exit(1);
            }
            int temp = 0;
            while(!table.containsinscope(temp,tok.string))
                temp++;
            var_name = "x_"+(table.getscope() - temp) + tok.string;
            System.out.print(var_name);
            scan();
        }
        else
            mustbe(TK.ID);
        System.out.print(";");
        
        int number = 0;
        if(is(TK.NUM))
        {
            number = Integer.parseInt(tok.string);
            scan();
        }
        else
            mustbe(TK.NUM);    
            
        if (is(TK.PLUS))
        {
            System.out.print(var_name + " < " + number + "; " + var_name + "++)"); 
            scan();
        } // while less then given number, increment 
        else if (is(TK.MINUS))
        {
            System.out.print(var_name + " > " + number + "; " + var_name + "--)"); 
            scan();
        } // while greater than given number, decrement 
        else
        {
            System.err.println( "mustbe: want + or -, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        } // error message, wrong token}: not +/-
        System.out.println("{");
        block();
        System.out.print("}");
    } // for loop 
}
     