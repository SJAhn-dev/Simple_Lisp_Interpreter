package interpreter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import parser.parse.*;
import parser.ast.*;
import java.util.Scanner;

public class CuteInterpreter {
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.print("> ");
			String input = sc.nextLine();
			try(FileWriter fw = new FileWriter("src/interpreter/LOG");
				PrintWriter pw = new PrintWriter(fw)){
				pw.write(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
			File file = new File("src/interpreter/LOG");
			
			CuteParser cuteParser = new CuteParser(file);
			CuteInterpreter interpreter = new CuteInterpreter();
			Node parseTree = cuteParser.parseExpr();
			Node resultNode = interpreter.runExpr(parseTree);
			NodePrinter nodePrinter = new NodePrinter(resultNode);
			System.out.print("... ");
			nodePrinter.prettyPrint();
		}
		
		// Item 1. Interpreter 구현
		// Input - Output 파일 입출력 대신 Console I/O를 사용합니다
		// 따라서 Scanner.netLine() 함수를 통하여 사용자의 입력을 받으며
		// 입력을 받으면 interpreter폴더에 LOG File을 생성하여
		// 사용자의 입력을 저장합니다. 이후 해당 File을 다시 읽어오는 방식을 통하여
		// CuteParse에 해당 file을 전달하고, 해당 구문을 Parsing한 후
		// Interpreter에 Parsing한 Token들을 보내 동작을 수행하여
		// 얻은 결과를 NodePrinter가 Console에 출력합니다.
	}
	
	private void errorLog(String err) {
		System.out.println(err);
	}
	
	public Node runExpr(Node rootExpr) {
		if (rootExpr == null)
			return null;
		if (rootExpr instanceof IdNode)
			return rootExpr;
		else if (rootExpr instanceof IntNode)
			return rootExpr;
		else if (rootExpr instanceof BooleanNode)
			return rootExpr;
		else if (rootExpr instanceof ListNode)
			return runList((ListNode) rootExpr);
		else
			errorLog("run Expr error");
		return null;
	}
	
	private Node runList(ListNode list) {
		list = (ListNode)stripList(list);
		if (list.equals(ListNode.EMPTYLIST))
			return list;
		if (list.car() instanceof FunctionNode) {
			if ( ((QuotableNode)list.car()).isQuoted() )
				return list;
			else {
				return runFunction((FunctionNode) list.car(), list.cdr());
			}			
		}
		if (list.car() instanceof BinaryOpNode) {
			if (((QuotableNode)list.car()).isQuoted())
				return list;
			else
				return runBinary(list);
		}
		return list;
	}
	
	private Node runFunction(FunctionNode operator, ListNode operand) {
		ListNode innerList;
		Node innerNode;
		switch (operator.funcType) {
			case CAR:
				if(operand == null || operand.car() == null) { break; }
				innerNode = operand.car();
				if(((ListNode) innerNode).car() instanceof QuoteNode) {
					innerNode = ((ListNode) innerNode).cdr();
					innerNode = ((ListNode) innerNode).car();
					innerNode = ((ListNode) innerNode).car();
				} else {
					innerNode = runList((ListNode)innerNode);
					innerNode = ((ListNode) innerNode).car();
				}
				return innerNode;
				// Function CAR
				// QuoteNode 내부의 List 값을 가져오기 위하여 
				// operand의 CAR ( ListNode ) -> CDR ( Car=QuoteNode)
				// -> CAR (ListNode) -> CAR ( ListNode 첫 번째 Node )
				// 순서로 불러온다.
				
			case CDR:
				if(operand == null || operand.car() == null) { break; }
				innerNode = operand.car();
				if(((ListNode) innerNode).car() instanceof QuoteNode) {
					innerNode = ((ListNode) innerNode).cdr();
					innerNode = ((ListNode) innerNode).car();
					innerNode = ((ListNode) innerNode).cdr();
				} else {
					innerNode = runList((ListNode)innerNode);
					innerNode = ((ListNode) innerNode).cdr();
				}
				
				if ( !(innerNode instanceof ListNode) ) { break; }
				if (innerNode == ListNode.ENDLIST ) { return ListNode.EMPTYLIST; }
				return innerNode;
				// Function CDR
				// QuoteNode 내부의 List 값을 가져오기 위하여 
				// operand의 CAR ( ListNode ) -> CDR ( Car=QuoteNode)
				// -> CAR (ListNode) -> CDR ( ListNode 두 번째 ~ 마지막 묶음 (ListNode) )
				// 순서로 불러온다.
				
			case CONS:
				if(operand == null || operand.car() == null) { break; }
				innerNode = operand.car();
				if(innerNode instanceof ListNode) {
					if(((ListNode)innerNode).car() instanceof QuoteNode) {
						innerNode = ((ListNode)innerNode).cdr().car();
					}
					else { innerNode = runList((ListNode)innerNode); }
				}
				innerList = operand.cdr();
				if(innerList.car() instanceof ListNode) {
					innerList = (ListNode) innerList.car();
					if(innerList.car() instanceof QuoteNode) {
						innerList = (ListNode)innerList.cdr().car();
					}
				}
				Node inner = runList((ListNode)innerList);
				return ListNode.cons(innerNode, (ListNode)inner);
				// Function CONS
				// 한 개의 원소와 리스트를 붙여 새로운 list를 만드는 함수
				// innerNode에 head ( Node or ListNode 판별 후 저장 )
				// innerList에 tail ( QuoteNode 판별 )
				// 이후 둘을 ListNode.cons로 합쳐 리턴
			
			case NULL_Q:
				if(operand == null || operand.car() == null) { break; }
				innerList = (ListNode) operand.car();
				if(innerList.car() instanceof QuoteNode) {
					innerList = innerList.cdr();
					innerNode = innerList.car();
					if((ListNode) innerNode == ListNode.ENDLIST) {
						return BooleanNode.TRUE_NODE;
					} else { return BooleanNode.FALSE_NODE; }
				}
				else {
					innerNode = runList(innerList);
					if(innerNode == null) { return BooleanNode.TRUE_NODE; }
					else { return BooleanNode.FALSE_NODE; }
				}
				// Function NULL_Q
				// car로 가져온 List를 QuoteNode 판별 후 null 여부를 판별한다
				// List에서 바로 ENDLIST가 나올경우 NULL로 판별한다
				
			case ATOM_Q:
				if(operand == null || operand.car() == null) { break; }
				innerList = (ListNode) operand.car();
				if(innerList.car() instanceof QuoteNode) {
					innerList = innerList.cdr();
					innerNode = innerList.car();
					if (!(innerNode instanceof ListNode)) { return BooleanNode.TRUE_NODE; }
					else {
						if(innerNode == ListNode.ENDLIST) {
							return BooleanNode.TRUE_NODE;
						} else { return BooleanNode.FALSE_NODE; }
					}
				} else {
					innerNode = runList(innerList);
					if (innerNode instanceof ListNode) { return BooleanNode.FALSE_NODE; }
					else { return BooleanNode.TRUE_NODE; }
				}
				// Function ATOM_Q
				// 원자를 판별하는 atom?, QuoteNode 판별 후 list 내부로 들어가고
				// list가 아니라면 바로 TRUE를 리턴하며 리스트일 경우 공백 List인 경우만
				// atom으로 판별한다
			
			case EQ_Q:
				if(operand == null || operand.car() == null) { break; }
				Node left = operand.car();
				Node right = operand.cdr().car();
				
				left = ((ListNode) left).cdr().car();
				right = ((ListNode) right).cdr().car();
				
				if(left.equals(right)) { return BooleanNode.TRUE_NODE; }
				else { return BooleanNode.FALSE_NODE; }
				// Function EQ_Q
				// Left와 Right를 판별후 equals 비교를 통하여 BooleanNode를 리턴한다
				
			case NOT:
				if(operand == null || operand.car() == null) { break; }
				innerNode = operand.car();
				
				if(innerNode instanceof ListNode) {
					innerNode = runList((ListNode) innerNode);
				}
				
				if(innerNode.equals(BooleanNode.TRUE_NODE)) { return BooleanNode.FALSE_NODE; }
				else { return BooleanNode.TRUE_NODE; }
				// Function NOT
				// List인 경우 내부 함수 실행을 통하여 연산 완료 후
				// 해당 Boolean값의 반대로 판별
				
			case COND:
				if(operand == null || operand.car() == null) { break; }
				innerList = (ListNode)operand.car();
				Node elseNode = operand.cdr().car();
				Node valueIf = innerList.cdr().car();
				Node valueElse = ((ListNode) elseNode).cdr().car();
				Node conditionNode = innerList.car();
				
				if(conditionNode instanceof ListNode) {
					conditionNode = runList((ListNode)conditionNode);
				}
				if(conditionNode.equals(BooleanNode.TRUE_NODE)) { return valueIf; }
				else { return valueElse; }
				// Function COND
				// 조건->conditionNode, else -> elseNode
				// valueIf , valueElse 판별 후 조건의 Boolean값에 따라서
				// valueif, valueelse를 리턴한다
				
			case DEFINE:
			case LAMBDA:
			
			default:
				break;
		}
		return null;
	}
	
	private Node stripList(ListNode node) {
		if (node.car() instanceof ListNode && node.cdr().car() == null) {
			Node listNode = node.car();
			return listNode;
		} else {
			return node;
		}
	}
	
	private Node runBinary(ListNode list) {
		BinaryOpNode operator = (BinaryOpNode) list.car();
		Node opLeft, opRight;
		int result;
		ListNode operand = list.cdr();
		switch (operator.binType) {
			case PLUS:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				result = ((IntNode) opLeft).getValue() + ((IntNode) opRight).getValue();
				return new IntNode( Integer.toString(result) );
				// Operator PLUS
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 value를 가져와 연산 후 새로운 IntNode의 값으로 저장해 리턴한다
				
			case MINUS:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				result = ((IntNode) opLeft).getValue() - ((IntNode) opRight).getValue();
				return new IntNode( Integer.toString(result) );
				// Operator MINUS
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 value를 가져와 연산 후 새로운 IntNode의 값으로 저장해 리턴한다
				
			case TIMES:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				result = ((IntNode) opLeft).getValue() * ((IntNode) opRight).getValue();
				return new IntNode( Integer.toString(result) );
				// Operator TIMES
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 value를 가져와 연산 후 새로운 IntNode의 값으로 저장해 리턴한다
				
			case DIV:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				result = ((IntNode) opLeft).getValue() / ((IntNode) opRight).getValue();
				return new IntNode( Integer.toString(result) );
				// Operator DIV
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 value를 가져와 연산 후 새로운 IntNode의 값으로 저장해 리턴한다
				
			case LT:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				if(((IntNode) opLeft).getValue() < ((IntNode) opRight).getValue()) {
					return BooleanNode.TRUE_NODE;
				} else { return BooleanNode.FALSE_NODE; }
				// Operator LT
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 비교 판별을 통하여 TRUE NODE혹은 FALSE NODE를 리턴한다
				
			case GT:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				if(((IntNode) opLeft).getValue() > ((IntNode) opRight).getValue()) {
					return BooleanNode.TRUE_NODE;
				} else { return BooleanNode.FALSE_NODE; }
				// Operator GT
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 비교 판별을 통하여 TRUE NODE혹은 FALSE NODE를 리턴한다
			
			case EQ:
				opLeft = operand.car();
				opRight = operand.cdr().car();
				
				if(opLeft instanceof ListNode)
					opLeft = runList((ListNode) opLeft);
				if(opRight instanceof ListNode)
					opRight = runList((ListNode) opRight);
				
				int opLeft_val = ((IntNode) opLeft).getValue();
				int opRight_val = ((IntNode) opRight).getValue();

				if(opLeft_val == opRight_val) {
					return BooleanNode.TRUE_NODE;
				} else { return BooleanNode.FALSE_NODE; }
				// Operator EQ
				// 좌항, 우항을 가져오고 해당 항들이 ListNode라면 내부 연산을 수행한다
				// 이후 두 값의 비교 판별을 통하여 TRUE NODE혹은 FALSE NODE를 리턴한다
			
			default:
				break;
		}
		return null;
	}

}
