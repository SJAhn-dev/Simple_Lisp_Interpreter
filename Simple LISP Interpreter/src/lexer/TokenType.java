package lexer;


public enum TokenType {
	INT,
	ID, 
	TRUE, FALSE, NOT,
	PLUS, MINUS, TIMES, DIV,   //special char
	LT, GT, EQ, APOSTROPHE,    //special char
	L_PAREN, R_PAREN,QUESTION, //special char
	DEFINE, LAMBDA, COND, QUOTE,
	CAR, CDR, CONS,
	ATOM_Q, NULL_Q, EQ_Q; 
	
	static TokenType fromSpecialCharactor(char ch) {
		switch ( ch ) {
			case '+': return PLUS;
			case '-': return MINUS;
			case '*': return TIMES;
			case '/': return DIV;
				
			case '(': return L_PAREN;
			case ')': return R_PAREN;
			case '?': return QUESTION;
			
			case '<': return LT;
			case '=': return EQ;
			case '>': return GT;
			case '\'': return APOSTROPHE;
			
			default:
				throw new IllegalArgumentException("unregistered char: " + ch);
				
			
			// Token Type Class 의 fromSpecialCharactor는
			// + - * / 와 같은 연산기호나 괄호와 같은 특수문자의
			// Token에 대한 Type을 리턴해주기 위하여 존재한다
		}
	}
}
