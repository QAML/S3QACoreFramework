package qa.qcri.iyas.data.preprocessing;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class StandardPreprocessor extends TextPreprocessor {
	
	private static final String WWW = "3WSUBS";

	private String unescape(String text) {
		Set<String> allMatches = new TreeSet<String>();
		Matcher m = Pattern.compile("&(\\s(\\d+));").matcher(text);
		while (m.find()) {
			allMatches.add(m.group());
		}
		
		for (String match : allMatches) {
			text = text.replace(match, match.replace(" ", "#"));
		}
		
		return StringEscapeUtils.unescapeHtml(text).replace("(", " ").replace(")", " ");
	}
	
	private String replaceArabicNumbers(String text) {
		text = text.replace("٠","0");
		text = text.replace("١","1");
		text = text.replace("٢","2");
		text = text.replace("٣","3");
		text = text.replace("٤","4");
		text = text.replace("٥","5");
		text = text.replace("٦","6");
		text = text.replace("٧","7");
		text = text.replace("٨","8");
		text = text.replace("٩","9");
		
		return text;
	}
	
	private String arPreprocess(String informalText){
		
		String normalizedText = replaceArabicNumbers(unescape(informalText));
		
		normalizedText = normalizedText.replace('؟', '?');
		
		normalizedText = normalizedText.replace('_', '-'); //substitutes _ with - in order not to mess with the stanford parser
		
		normalizedText = normalizedText.replaceAll("!+", "!");//eliminates multiple occurrences of !
		
		normalizedText = normalizedText.replaceAll("[?]+", "?");//eliminates multiple occurrences of ?
		
		normalizedText = normalizedText.replaceAll("([!]*[?]+[!]*)+", "?");//substitute ?!? into ?
		
		normalizedText = normalizedText.replaceAll("[.]+", ".");//eliminates multiple occurrences of .
	
		normalizedText = normalizedText.replaceAll("\\[[^\\]\\[]*\\]", "");//eliminates text in squared brackets (they usually are not informative and they confuse the parser)
		
		normalizedText = normalizedText.replaceAll(";", " ").replace("+", " ");//Farasa segmenter doesn't avoid this character, Farasa POS tagger does.
		
		normalizedText = normalizedText.replace("(", " ( ").replace(")", " ) ").trim();

		//normalizedText.replaceAll("\[[^\[\]]*\]", "");
		
	//	normalizedText = normalizedText.replace("E", "e");
	//	normalizedText = normalizedText.replace("S", "s");
		
		normalizedText = normalizedText.toLowerCase();
		
//		char chars[] = normalizedText.toCharArray();
//		for (int i=0;i<chars.length;i++)
//			if (chars[i] == '(' || chars[i] == ')')
////				chars[i] = ' ';
//		
//		return new String(chars);
		
		return normalizedText;
	}
	
	private String enPreprocess(String informalText){
		String normalizedText = informalText.replaceAll("www", WWW);//for preserving the www (otherwise they will be shrinked in a single 'w')

		normalizedText = getText(Jsoup.parse(normalizedText));
		normalizedText = getText(Jsoup.parse(normalizedText));//html tag removal (it is done twice cause in case of tags written in a format like &amp;lt;P&amp;gt; the will be firstly converted into <p> by the first call, while the second will actually remove the tags 
		
		normalizedText = normalizedText.replace('_', '-'); //substitutes _ with - in order not to mess with the stanford parser
		
		normalizedText = normalizedText.replaceAll("!+", "!");//eliminates multiple occurrences of !
		
		normalizedText = normalizedText.replaceAll("[?]+", "?");//eliminates multiple occurrences of ?
		
		normalizedText = normalizedText.replaceAll("([!]*[?]+[!]*)+", "?");//substitute ?!? into ?
		
		normalizedText = normalizedText.replaceAll("[.]+", ".");//eliminates multiple occurrences of .
		
		normalizedText = normalizedText.replaceAll("([a-zA-Z])(\\1{2,})", "$1");//substitutes triple (or more) occurrences of the same letter with a single occurrence
		
		normalizedText = normalizedText.replaceAll("\\[[^\\]\\[]*\\]", "");//eliminates text in squared brackets (they usually are not informative and they confuse the parser)
		
		normalizedText = normalizedText.replace("(", " ( ").replace(")", " ) ").trim();

		//normalizedText.replaceAll("\[[^\[\]]*\]", "");
		
		
		
		if(normalizedText.equals(normalizedText.toUpperCase())){
			normalizedText = normalizedText.toLowerCase();
		}
		
		return normalizedText.replaceAll(WWW, "www");
	}
	
	/**
	 * @param cell element that contains whitespace formatting
	 * @return
	 */
	private String getText(org.jsoup.nodes.Element cell) {
	    String text = null;
	    List<Node> childNodes = cell.childNodes();
	    if (childNodes.size() > 0) {
	        Node childNode = childNodes.get(0);
	        if (childNode instanceof TextNode) {
	            text = ((TextNode)childNode).getWholeText();
	        }
	    }
	    if (text == null) {
	        text = cell.text();
	    }
	    return text;
	}
	
//	private String removeMultipleWhiteSpaces(String string){
//		char noBreakSpace = (char)160;
//		return string.replaceAll("[\r \t\f"+ noBreakSpace +"\r]+", " ").replaceAll("(( )*\n( )*)+", "\n").trim();
//	}
	
	@Override
	public String preprocess(String text, String lang) {
		if (lang.equals("en"))
			return enPreprocess(text);
		else if (lang.equals("ar"))
			return arPreprocess(text);
		
		return null;
	}

}
