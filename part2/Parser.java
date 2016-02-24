   /* *** This file is given as part of the programming assignment. *** */

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	    tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
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
	} // if not correct token, print error message 
	scan();
    }
    
    
    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    } // print line number of error message 
    
    private void program() {
	block();
    }

    private void block(){
	declaration_list();
	statement_list();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
    	while( is(TK.DECLARE) ) {
    	    declaration();
    	    }
    }

    private void declaration() {
    	mustbe(TK.DECLARE);
    	mustbe(TK.ID);
    	while( is(TK.COMMA) ) {
    	    scan();
    	    mustbe(TK.ID);
    	    }
    }

    private void statement_list()
    {
        while (is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)) 
            statement();
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
    }//statement is either var, or print, or new block, or if 
    
    private void print()
    {
        mustbe(TK.PRINT);
        expr();
    } // print expression 
    
    
    private void assignment()
    {
        ref_id();
        mustbe(TK.ASSIGN); 
        expr();
    } // assign value to and id 
    
    private void ref_id()
    {
        if (is(TK.TILDE)) //if there is a tilde
        {
            mustbe(TK.TILDE); //must be tilde 
            if (is(TK.NUM))
            {
                mustbe(TK.NUM);
            } //could or could not have num
        }
        mustbe(TK.ID);
        
    }
    
    private void do_this ()
    {
        mustbe(TK.DO);
        guarded_command();
        mustbe(TK.ENDDO);
    }
    
    //not sure
    private void if_this ()
    {
        if (is(TK.IF))
        {
            scan();
            guarded_command();
            while (is(TK.ELSEIF))
            {
                mustbe(TK.ELSEIF);
                guarded_command();
            }//else if 
        }//if then, expression
        if(is(TK.ELSE))
        {
            mustbe(TK.ELSE);
            block();
        }//else to the if  
        mustbe(TK.ENDIF); //end token for if statement 
    }
    
    private void guarded_command()
    {
        expr();
        mustbe(TK.THEN);
        block();
    }
    
    private void expr()
    {
        term();
        while ( is(TK.PLUS) || is(TK.MINUS)) // either add or minus 
        {
            addop();
            term();
        }
    } // term +/- term 
    
    private void term()
    {
        factor();
        while (is(TK.TIMES) || is(TK.DIVIDE)) // either mult, or div
        {
            multop();
            factor();
        }
    } //factor * / factor 
    
    //not sure
    private void factor()
    {
        if (is(TK.LPAREN))
        {
            scan();
            expr();
            mustbe(TK.RPAREN);
        }
         
        else if(is(TK.TILDE) || is(TK.ID))
            ref_id();
        else if(is(TK.NUM))
            scan();
        else
        {
            System.err.println( "mustbe: want factor, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        } // if not ~, if not num, then print error msg 
    }
    
    private void addop()
    {
        if (is(TK.PLUS) || is(TK.MINUS))
            scan();
        else
        {
            System.err.println( "mustbe: want + or -, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        } //if not +/- then print error msg 
    }       
    
    private void multop()
    {
        if (is(TK.TIMES) || is(TK.DIVIDE))
            scan();
        else
        {
            System.err.println( "mustbe: want * or /, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        } // if not */ then print error msg 
    }
}
