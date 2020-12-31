package parser.parse;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import parser.ast.*;

public class NodePrinter {
	private final String OUTPUT_FILENAME = "output07.txt";
	private StringBuffer sb = new StringBuffer();
	private Node root;
	public NodePrinter(Node root){
		this.root = root;
	}

	private void printList(ListNode listNode) {
		if (listNode == ListNode.EMPTYLIST) {
			sb.append("( )");
			return;
		}
		if (listNode == ListNode.ENDLIST) {
			return;
		}
		
		if (listNode.car() instanceof QuoteNode) {
			ListNode ln = listNode;
			while(ln != ListNode.ENDLIST) {
				Node nd = ln.car();
				printNode(nd);
				ln = ln.cdr();
			}
			return;
		} else {
			sb.append("( ");
			ListNode ln = listNode;
			while(ln != ListNode.ENDLIST) {
				Node nd = ln.car();
				printNode(nd);
				ln = ln.cdr();
			}
			sb.append(") ");
		}
		
		// EMPTYLIST나 ENDLIST가 아닌 listNode의 출력부분입니다.
		// 이미 QuoteNode 이후 부분에 listNode가 존재하므로,
		// .car()가 QuoteNode의 instance(객체)라면
		// 괄호를 따로 출력하지 않고 출력합니다.
		// 만약 일반적인 listNode라면 괄호를 출력한 뒤
		// listNode내부의 node들을 car(), cdr()를 사용하여
		// ENDLIST까지 순회한 뒤 마지막으로 괄호를 닫아줍니다.
	}

	private void printNode(Node node) {
		if (node == null) 
			return;
		if (node instanceof ListNode) {
			ListNode ln = (ListNode) node;
			printList(ln);
		} else if (node instanceof QuoteNode) {
			sb.append(node);
		} else {
			sb.append("[" + node + "] ");
		}
		
		// Node를 Print하는 부분입니다.
		// Apostrophe (quoteNode)는 대괄호를 필요로
		// 하지 않으므로 해당 node (`)만 출력합니다.
		// 나머지 Node들은 대괄호를 붙여 정상적으로 print합니다
	}
   
	public void prettyPrint(){
		printNode(root);
//		try(FileWriter fw = new FileWriter(OUTPUT_FILENAME);
//			PrintWriter pw = new PrintWriter(fw)){
//			pw.write(sb.toString());
//		}catch (IOException e){
//			e.printStackTrace();
//		}
		System.out.println(sb.toString());
	}
}
