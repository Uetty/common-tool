package com.uetty.common.tool.core.string;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class HtmlUtil {

    static Map<String, String> ESCAPE_HTML_REPLACE_MAP = null;
    static Map<String, String> ESCAPE_SCRIPT_REPLACE_MAP = null;
    private static void initEscapeHTMLMapping() {
        if (ESCAPE_HTML_REPLACE_MAP != null) return;
        synchronized (HtmlUtil.class) {
            if (ESCAPE_HTML_REPLACE_MAP != null) return;

            Map<String, String> htmlMap = new LinkedHashMap<>();
            // 标签包含包裹内容一起被删掉的标签名
            String[] fullEscapeTag = new String[]{
                    "head", "title", "script", "video", "audio", "style", "colgroup", "select", "img", "option",
                    "optgroup", "link", "meta"
            };
            // 头标签被替换成换行符的标签名
            String[] escapeByLFTag = new String[]{
                    "div", "p", "br", "iframe", "html", "table", "thead", "tr", "hr",
                    "h1", "h2", "h3", "h4", "h5", "li", "tbody"
            };
            // 头标签与尾标签被替换成空格的标签名
            String[] escapeByBlankTag = new String[]{
                    "a", "td", "th", "blockquote", "form", "nav", "code", "body"
            };
            // 其他头标签与尾标签替换成空字符的标签名
            String[] otherTag = new String[] {
                    "span", "label", "ul", "ol", "u", "col", "b", "input", "button", "i"
            };
            htmlMap.put("(?s)<!--.*?-->", "");
            // 标签包含包裹内容一起被删掉的匹配规则
            StringBuilder escape1 = new StringBuilder();
            escape1.append("(?i)(?s)"); // 大小写不敏感，.匹配行终止符
            for (String tag : fullEscapeTag) {
                escape1.append("(<").append(tag).append("([ ]+[^ >]+?)*[ ]*>.*?</").append(tag).append(">)");
                escape1.append("|");
            }
            escape1.delete(escape1.length() - 1, escape1.length());
            htmlMap.put(escape1.toString(), "");
            // 替换成换行符的匹配规则
            StringBuilder escape2 = new StringBuilder();
            escape2.append("(?i)(?s)");
            for (String tag : escapeByLFTag) {
                escape2.append("(<").append(tag).append("([ ]+[^ >]+?)*[ ]*[/]?>)");
                escape2.append("|");
            }
            escape2.delete(escape2.length() - 1, escape2.length());
            htmlMap.put(escape2.toString(), "\n");
            // 替换成空格的匹配规则
            StringBuilder escape3 = new StringBuilder();
            escape3.append("(?i)(?s)");
            for (String tag : escapeByBlankTag) {
                escape3.append("(<").append(tag).append("([ ]+[^ >]+?)*[ ]*[/]?>)|(</").append(tag).append(">)");
                escape3.append("|");
            }
            escape3.delete(escape3.length() - 1, escape3.length());
            htmlMap.put(escape3.toString(), "  ");
            // 替换成空字符的匹配规则
            StringBuilder escape4 = new StringBuilder();
            escape4.append("(?i)(?s)");
            for (String tag : otherTag) {
                escape4.append("(<").append(tag).append("([ ]+[^ >]+?)*[ ]*[/]?>)|(</").append(tag).append(">)");
                escape4.append("|");
            }
            for (String tag : fullEscapeTag) {// 防止遗漏的只有标签头/尾 的标签
                escape4.append("(<").append(tag).append("([ ]+[^ >]+?)*[ ]*[/]?>)|(</").append(tag).append(">)");
                escape4.append("|");
            }
            for (String tag : escapeByLFTag) {
                escape4.append("(</").append(tag).append(">)");
                escape4.append("|");
            }
            escape4.delete(escape4.length() - 1, escape4.length());
            htmlMap.put(escape4.toString(), "");
            ESCAPE_HTML_REPLACE_MAP = htmlMap;
        }
    }
    private static void initEscapeScriptMapping() {
        if (ESCAPE_SCRIPT_REPLACE_MAP != null) return;
        synchronized (HtmlUtil.class) {
            if (ESCAPE_SCRIPT_REPLACE_MAP != null) return;
            Map<String, String> scriptMap = new LinkedHashMap<>();
            // 去除脚本
            scriptMap.put("(?s)<!--.*?-->", "");
			String scriptEscape = "(?i)(?s)" +
					"(<script([ ]+[^ >]+?)*[ ]*>.*?</script>)" +
					"|" +
					"(<link([ ]+[^ >]+?)*[ ]*>.*?</link>)" +
					"|" +
					"(<style([ ]+[^ >]+?)*[ ]*>.*?</style>)" +
					"|" +
					"(<meta([ ]+[^ >]+?)*[ ]*>.*?</meta>)";
			scriptMap.put(scriptEscape, "");
            ESCAPE_SCRIPT_REPLACE_MAP = scriptMap;
        }
    }
    /**
     * 去除HTML标签
	 * @param text html
	 * @return 去除标签后
     */
    public static String escapeHtml(String text) {
        initEscapeHTMLMapping();

		for (String key : ESCAPE_HTML_REPLACE_MAP.keySet()) {
			text = text.replaceAll(key, ESCAPE_HTML_REPLACE_MAP.get(key));
		}
        text = text.replace("<", "&lt;").replace(">", "&gt;");
        text = text.replaceAll("\n+", "\n");
        return text;
    }
    /**
     * 去除脚本标签
	 * @param text 可能包含脚本的字符串
	 * @return 去除可能包含的脚本
     */
    public static String escapeScript(String text) {
        initEscapeScriptMapping();

		for (String key : ESCAPE_SCRIPT_REPLACE_MAP.keySet()) {
			text = text.replaceAll(key, ESCAPE_SCRIPT_REPLACE_MAP.get(key));
		}
        text = text.replaceAll("\n+", "\n");
        return text;
    }

    /**
     * 标签区间块
     */
    private static class Block {
        String openKey; // 标签开始位置键名
        String closeKey; // 标签结束位置键名（单标签的标签，这一个为null）
    }

    /**
     * 查找html代码的指定标签，并根据函数替换内容
	 * @param html html
	 * @param tagName 标签名
	 * @param mapper 针对该标签的处理
	 * @return 处理这些标签后的文本
     */
    public static String tagReplace(String html, String tagName, BiFunction<String, String, String> mapper) {
        Objects.requireNonNull(mapper);

        String openRegex = "(?i)(?s)(<" + tagName + "([ ]+[^ >]+?)*[ ]*[/]?>)";
        String closeRegex = "</" + tagName + ">";

        List<String> openKeys = new ArrayList<>();
        List<String> closeKeys = new ArrayList<>();
        Map<String, String> markMap = new HashMap<>(); // 标记字符串和原始开闭标签字符串的映射

        Pattern op = Pattern.compile(openRegex);
        Matcher matcher = op.matcher(html);
        // 将开标签<span style="">的位置替换为标记字符串
        html = markTag(html, op, matcher, markMap, openKeys);

        Pattern cp = Pattern.compile(closeRegex);
        matcher = cp.matcher(html);
        // 将闭标签</span>的位置替换为标记字符串
        html = markTag(html, cp, matcher, markMap, closeKeys);

        // 根据标记字符串定位html开标签（<span>）的位置，方便后面计算区间
        int[] openLocates = locateKeys(html, openKeys);
        // 根据标记字符串定位html闭标签(</span>)的位置，方便后面计算区间
        int[] closeLocates = locateKeys(html, closeKeys);
        // 获取完整标签（包含开闭标签）区间
        List<Block> blocks = loadBlocks(html, openKeys, closeKeys, openLocates, closeLocates);

        StringBuilder sb = new StringBuilder();
        sb.append(html);
        blocks.forEach(block -> {
            String headTag = markMap.remove(block.openKey);
            String tailTag = block.closeKey != null ? markMap.remove(block.closeKey) : null;

            String fullHtml = sb.toString();
            int openIdx = fullHtml.indexOf(block.openKey);
            int closeIdx = tailTag == null ? -1 : fullHtml.indexOf(block.closeKey);
            String fullTag = headTag + (closeIdx == -1 ? "" : fullHtml.substring(openIdx + block.openKey.length(), closeIdx) + tailTag);

            String apply = mapper.apply(fullTag, headTag);

            fullHtml = fullHtml.substring(0, openIdx) + apply + fullHtml.substring(closeIdx == -1 ? openIdx + block.openKey.length() : closeIdx + block.closeKey.length());
            sb.delete(0, sb.length());
            sb.append(fullHtml);
        });
        String text = sb.toString();
        if (markMap.size() > 0) {
            Set<Map.Entry<String, String>> entries = markMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }

    /**
     * 根据开闭标签的位置计算装载html标签键名区间块，方便后面按顺序进行replace工作
	 * @param html html
	 * @param openKeys 区块标签头部内容替换后的id值
	 * @param closeKeys 区块标签尾部内容替换后的id值
	 * @param openLocates 标签头位置值栈
	 * @param closeLocates 标签尾位置值栈
	 * @return 区块列表
     */
    private static List<Block> loadBlocks(String html, List<String> openKeys, List<String> closeKeys, int[] openLocates, int[] closeLocates) {
        List<Block> blocks = new ArrayList<>();

        Arrays.sort(openLocates);
        Arrays.sort(closeLocates);

        List<Integer> openStack = new ArrayList<>();
        for (int i = 0, k = 0; i < openLocates.length || k < closeLocates.length; ) {
            boolean nextCloseMark = i >= openLocates.length || (k < closeLocates.length && closeLocates[k] < openLocates[i]); // 下一个标签是close

            if (nextCloseMark) { // 下一个标签是close，如：</span>
                if (openStack.size() > 0) {
                    int ii = openStack.remove(openStack.size() - 1);
                    Block block = new Block();
                    block.openKey = openKeys.get(ii);
                    block.closeKey = closeKeys.get(k);
                    blocks.add(block);
		}
                k++;
            } else { // 下一个标签是open，如：<span>
                openStack.add(i);
                i++;
            }
        }
        for (int j = openStack.size() - 1; j >= 0; j--) {
            Block block = new Block();
            block.openKey = openKeys.get(openStack.get(j));
            blocks.add(block);
        }
        return blocks;
    }

    /**
     * 定位键名在文本中的位置
	 * @param html html
	 * @param keys 替换后的id值列表
	 * @return int[] 位置列表
     */
    private static int[] locateKeys(String html, List<String> keys) {
        int[] locate = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            locate[i] = html.indexOf(keys.get(i));
        }
        return locate;
    }

    /**
     * 根据匹配规则找到匹配的内容，并分别替换为不重复的序列，记录替换后序列位置
	 * @param html 原始html
	 * @param op 匹配规则
	 * @param matcher matcher实例
	 * @param markMap 替换后id与原始字符串映射
	 * @param keys 替换后id列表
	 * @return 替换后html
     */
    private static String markTag(String html, Pattern op, Matcher matcher, Map<String, String> markMap, List<String> keys) {
        while (matcher.find()) {
            String group = matcher.group();
            String markKey = "<mark uuid=\"" + UUID.randomUUID() + "\">";
            int i = html.indexOf(group);
            // 将标签的位置替换成自定义的标记
            html = html.substring(0, i) + markKey + html.substring(i + group.length());
            matcher = op.matcher(html);
            markMap.put(markKey, group);
            keys.add(markKey);
        }
        return html;
    }
	
	public static void main(String[] args) {
//		String str = "<p>1234</p><p>&lt;p&gt;哈哈哈哈&lt;/p&gt;</p><ol><li>&lt;script&gt;个为而非哇个&lt;/script&gt;<br/><span style=\"font-size: 1.5em;\">gwafwfaherdsfgw<br>hea<span style=\"color: rgb(226, 139, 65);\">彩色文<a href=\"http://www.example.com\" target=\"_blank\">wg</a>字gwe</span></span></li></ol><blockquote><p>gawefg</p></blockquote><p><u>gwafewf&nbsp;<br>hg链接文字个为额发</u></p><p><u><br></u></p><table><colgroup><col width=\"24.92581602373887%\"><col width=\"25.024727992087044%\"><col width=\"25.024727992087044%\"><col width=\"25.222551928783382%\"></colgroup><thead><tr><th>table</th><th>blee</th><th>tabl</th><th>tab</th></tr></thead><tbody><tr><td>1</td><td>1</td><td>1</td><td>1</td></tr><tr><td>2</td><td>2</td><td>2</td><td>2</td></tr><tr><td>3</td><td>3</td><td>3</td><td>3<br><br></td></tr></tbody></table><p style=\"text-align: right;\">gwgweewaef</p><p style=\"margin-left: 40px;\"><img alt=\"Image\"><br><br></p><hr><h2>gwefagwe<b>gfaef</b></h2><ul><li>wwgwe</li><li>gwe</li><li>wegwa</li><li>wg<br></li></ul><ol><li>gwefa</li><li>sf<br><br><br></li></ol><p>gwaefof</p><ol><li>1. gwefa</li><li>2. wgwef</li><li>3. dgwe<br><br></li></ol>";
//		String str = "表单记录:【版本号】V1.0.4.5【测试包存放位置】<dIv>用</div>火<p g=\"gwe\"  m='gwe'>gwef</p>狐测<br>一<span style=\"color: red;\">次</span>【建议测试内容】<bigtext>火狐来测一测\n" + 
//				"火狐来测一测\n" + 
//				"\n" + 
//				"火狐来测一测\n" + 
//				"火狐来测一测\n" + 
//				"火狐来测一测\n" + 
//				"火狐来测一测\n" + 
//				"火狐来测一测\n" + 
//				"火狐来测一测</bigtext>";
		String str = "<div class=\"ace_layer ace_text-layer\" style=\"padding: 0px 4px;\"><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_identifier\">r</span>.<span class=\"ace_identifier\">role_code</span>,</div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_identifier\">r</span>.<span class=\"ace_identifier\">role_name</span>,</div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_identifier\">training</span>.* </div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_keyword\">FROM</span> <span class=\"ace_constant ace_buildin\">`pub_t_hcpe_attendee_training`</span> <span class=\"ace_identifier\">training</span></div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_keyword\">LEFT</span> <span class=\"ace_keyword\">JOIN</span> <span class=\"ace_identifier\">pub_sys_user</span> <span class=\"ace_identifier\">u</span></div><div class=\"ace_line\" style=\"height:19px\">    <span class=\"ace_keyword\">ON</span> <span class=\"ace_identifier\">u</span>.<span class=\"ace_identifier\">username</span> <span class=\"ace_keyword ace_operator\">=</span> <span class=\"ace_identifier\">training</span>.<span class=\"ace_identifier\">create_by</span></div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_keyword\">LEFT</span> <span class=\"ace_keyword\">JOIN</span> <span class=\"ace_identifier\">pub_sys_user_role</span> <span class=\"ace_identifier\">ur</span></div><div class=\"ace_line\" style=\"height:19px\">    <span class=\"ace_keyword\">ON</span> <span class=\"ace_identifier\">ur</span>.<span class=\"ace_identifier\">user_id</span> <span class=\"ace_keyword ace_operator\">=</span> <span class=\"ace_identifier\">u</span>.<span class=\"ace_identifier\">id</span></div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_keyword\">LEFT</span> <span class=\"ace_keyword\">JOIN</span> <span class=\"ace_identifier\">pub_sys_role</span> <span class=\"ace_identifier\">r</span></div><div class=\"ace_line\" style=\"height:19px\">    <span class=\"ace_keyword\">ON</span> <span class=\"ace_identifier\">r</span>.<span class=\"ace_identifier\">id</span> <span class=\"ace_keyword ace_operator\">=</span> <span class=\"ace_identifier\">ur</span>.<span class=\"ace_identifier\">role_id</span></div><div class=\"ace_line\" style=\"height:19px\">    <span class=\"ace_keyword\">AND</span> <span class=\"ace_identifier\">r</span>.<span class=\"ace_identifier\">role_code</span> <span class=\"ace_keyword\">IN</span> <span class=\"ace_paren ace_lparen\">(</span><span class=\"ace_string ace_start\">'</span><span class=\"ace_string\">EO</span><span class=\"ace_string ace_end\">'</span>, <span class=\"ace_string ace_start\">'</span><span class=\"ace_string\">MDT</span><span class=\"ace_string ace_end\">'</span><span class=\"ace_paren ace_rparen\">)</span></div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_keyword\">WHERE</span> </div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_constant ace_numeric\">1</span> <span class=\"ace_keyword ace_operator\">=</span> <span class=\"ace_constant ace_numeric\">1</span></div><div class=\"ace_line\" style=\"height:19px\"><span class=\"ace_keyword\">ORDER</span> <span class=\"ace_support ace_function\">BY</span> <span class=\"ace_identifier\">training</span>.<span class=\"ace_constant ace_buildin\">`id`</span>, <span class=\"ace_keyword\">case</span> <span class=\"ace_identifier\">r</span>.<span class=\"ace_identifier\">role_code</span> <span class=\"ace_keyword\">WHEN</span> <span class=\"ace_string ace_start\">'</span><span class=\"ace_string\">MDT</span><span class=\"ace_string ace_end\">'</span> <span class=\"ace_keyword\">THEN</span> <span class=\"ace_constant ace_numeric\">1</span> <span class=\"ace_keyword\">WHEN</span> <span class=\"ace_string ace_start\">'</span><span class=\"ace_string\">EO</span><span class=\"ace_string ace_end\">'</span> <span class=\"ace_keyword\">THEN</span> <span class=\"ace_constant ace_numeric\">2</span> <span class=\"ace_keyword\">ELSE</span> <span class=\"ace_constant ace_numeric\">3</span> <span class=\"ace_keyword\">END</span> <span class=\"ace_keyword\">ASC</span></div><div class=\"ace_line\" style=\"height:19px\"></div></div>";
		String copyStr = str;
		str = escapeHtml(str.replace("\n", " "));
		str = str.replaceAll("[\\n][\\s]*[\\n]", "\n").replaceAll("[ \t]{3,}", "  ");
//		str = str.replaceAll("[\\s]", "");
		System.out.println(str);
		
		String tagReplace = tagReplace(copyStr, "table", (fullTag, headTag) -> "TABLE DELETED");
		System.out.println();
		System.out.println("replace ====> ");
		System.out.println();
		System.out.println(tagReplace);
	}

}
