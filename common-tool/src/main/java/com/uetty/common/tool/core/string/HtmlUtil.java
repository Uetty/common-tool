package com.uetty.common.tool.core.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                escape1.append("(<" + tag + "([ ]+[^ >]+?)*[ ]*>.*?</" + tag + ">)");
                escape1.append("|");
            }
            escape1.delete(escape1.length() - 1, escape1.length());
            htmlMap.put(escape1.toString(), "");
            // 替换成换行符的匹配规则
            StringBuilder escape2 = new StringBuilder();
            escape2.append("(?i)(?s)");
            for (String tag : escapeByLFTag) {
                escape2.append("(<" + tag + "([ ]+[^ >]+?)*[ ]*[/]?>)");
                escape2.append("|");
            }
            escape2.delete(escape2.length() - 1, escape2.length());
            htmlMap.put(escape2.toString(), "\n");
            // 替换成空格的匹配规则
            StringBuilder escape3 = new StringBuilder();
            escape3.append("(?i)(?s)");
            for (String tag : escapeByBlankTag) {
                escape3.append("(<" + tag + "([ ]+[^ >]+?)*[ ]*[/]?>)|(</" + tag + ">)");
                escape3.append("|");
            }
            escape3.delete(escape3.length() - 1, escape3.length());
            htmlMap.put(escape3.toString(), "  ");
            // 替换成空字符的匹配规则
            StringBuilder escape4 = new StringBuilder();
            escape4.append("(?i)(?s)");
            for (String tag : otherTag) {
                escape4.append("(<" + tag + "([ ]+[^ >]+?)*[ ]*[/]?>)|(</" + tag + ">)");
                escape4.append("|");
            }
            for (String tag : fullEscapeTag) {// 防止遗漏的只有标签头/尾 的标签
                escape4.append("(<" + tag + "([ ]+[^ >]+?)*[ ]*[/]?>)|(</" + tag + ">)");
                escape4.append("|");
            }
            for (String tag : escapeByLFTag) {
                escape4.append("(</" + tag + ">)");
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
            StringBuilder scriptEscape = new StringBuilder();
            scriptEscape.append("(?i)(?s)");
            scriptEscape.append("(<script([ ]+[^ >]+?)*[ ]*>.*?</script>)");
            scriptEscape.append("|");
            scriptEscape.append("(<link([ ]+[^ >]+?)*[ ]*>.*?</link>)");
            scriptEscape.append("|");
            scriptEscape.append("(<style([ ]+[^ >]+?)*[ ]*>.*?</style>)");
            scriptEscape.append("|");
            scriptEscape.append("(<meta([ ]+[^ >]+?)*[ ]*>.*?</meta>)");
            scriptMap.put(scriptEscape.toString(), "");
            ESCAPE_SCRIPT_REPLACE_MAP = scriptMap;
        }
    }
    /**
     * 去除HTML标签
     * @Author : Vince
     */
    public static String escapeHtml(String text) {
        initEscapeHTMLMapping();

        Iterator<String> keyItr = ESCAPE_HTML_REPLACE_MAP.keySet().iterator();
        while (keyItr.hasNext()) {
            String key = keyItr.next();
            text = text.replaceAll(key, ESCAPE_HTML_REPLACE_MAP.get(key));
        }
        text = text.replace("<", "&lt;").replace(">", "&gt;");
        text = text.replaceAll("\n+", "\n");
        return text;
    }
    /**
     * 去除脚本标签
     * @Author : Vince
     */
    public static String escapeScript(String text) {
        initEscapeScriptMapping();

        Iterator<String> keyItr = ESCAPE_SCRIPT_REPLACE_MAP.keySet().iterator();
        while (keyItr.hasNext()) {
            String key = keyItr.next();
            text = text.replaceAll(key, ESCAPE_SCRIPT_REPLACE_MAP.get(key));
        }
        text = text.replaceAll("\n+", "\n");
        return text;
    }

    /**
     * 标签区间块
     * @Author : Vince
     */
    private static class Block {
        String openKey; // 标签开始位置键名
        String closeKey; // 标签结束位置键名（单标签的标签，这一个为null）
    }

    /**
     * 查找html代码的指定标签，并根据函数替换内容
     * @Author : Vince
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
     * @Author : Vince
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
     * @Return : int[] 位置列表
     * @Author : Vince
     */
    private static int[] locateKeys(String html, List<String> keys) {
        int[] locate = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            locate[i] = html.indexOf(keys.get(i));
        }
        return locate;
    }

    /**
     * 标记html标签的位置
     * @Author : Vince
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
		String str = "<div id=\"softverDetails\" class=\"modal fade history-modal in\" style=\"display: block;\" aria-hidden=\"false\">\n" + 
				"    <div class=\"modal-dialog\">\n" + 
				"        <div class=\"modal-content\">\n" + 
				"            <div class=\"modal-header\">\n" + 
				"                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">×</button>\n" + 
				"                <h3 class=\"modal-title\">提测版本详情</h3>\n" + 
				"            </div>\n" + 
				"            <div class=\"modal-body\">\n" + 
				"                <ul id=\"softver_details_nav\" class=\"nav nav-tabs\">\n" + 
				"                    <li class=\"active\"><a id=\"softver_details_nav_base_info\" href=\"#softver_details_base_info\" data-toggle=\"tab\">基本信息</a></li>\n" + 
				"                    <li><a id=\"softver_details_nav_rls_info\" href=\"#softver_details_rls_info\" data-toggle=\"tab\" class=\"hidden\">发布信息</a></li>\n" + 
				"                </ul>\n" + 
				"                <div class=\"tab-content\">\n" + 
				"                    <div id=\"softver_details_base_info\" class=\"tab-pane fade active in\" style=\"position: relative;\">\n" + 
				"                        <table class=\"details-table\" style=\"width: 100%;\">\n" + 
				"                            <thead>\n" + 
				"                                <tr>\n" + 
				"                                    <th style=\"width: 25%;\"></th>\n" + 
				"                                    <th style=\"width: 20%;\"></th>\n" + 
				"                                    <th style=\"width: 20%;\"></th>\n" + 
				"                                    <th style=\"width: 35%;\"></th>\n" + 
				"                                </tr>\n" + 
				"                            </thead>\n" + 
				"                            <tbody>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">项目：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_proj\" colspan=\"3\">SSR</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">标题：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_title\" colspan=\"3\">湖北-V3.9.0.3-胡建</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">提测版本号：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_number\" colspan=\"3\">V3.9.0.3</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">状态：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_state\" colspan=\"3\">提测评审中</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">提交人：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_cuser\" colspan=\"3\">方文城</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\" id=\"ti_owne\">当前负责人：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_owner\" colspan=\"3\">蒋玉莹</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">提测时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_ctime\">2018-08-31</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">修改时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_utime\">2018-08-31</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">测试包存放地址：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_pkgurl\">我噶发</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">建议测试内容：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_tcontent\">我无法</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">测试计划存放地址：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_tpurl\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">测试计划附件：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\">\n" + 
				"                                        <ul id=\"softver_details_tpfile\"></ul>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">测试报告地址：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_trurl\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">测试报告附件：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\">\n" + 
				"                                        <ul id=\"softver_details_trfile\"></ul>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">测试计划变更次数：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_tpchange\">0</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">完成测试时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_ttime\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">版本评估时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_eetime\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">版本评估评定：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_ejudge\">待定</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">评估决议存放地址：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_erurl\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">评估决议附件：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\">\n" + 
				"                                        <ul id=\"softver_details_erfile\"></ul>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">试商用开始时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_bstime\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">试商用结束时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_betime\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">试商用评定：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_bjudge\">待定</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">试商用附件：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\">\n" + 
				"                                        <ul id=\"softver_details_bfile\"></ul>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                            </tbody>\n" + 
				"                        </table>\n" + 
				"    \n" + 
				"                        <!-- box 历史记录  collapse-history -->\n" + 
				"                        <div class=\"div_history\">\n" + 
				"                            <div class=\"panel-heading panel-border-hidden mouse-hand collapsed\" role=\"tab\" data-toggle=\"collapse\" href=\"#collapse-history\" aria-expanded=\"true\" aria-controls=\"collapse-history\" style=\"padding-left: 0px;\">\n" + 
				"                                <h4 class=\"panel-title\" style=\"padding: 0px;\">\n" + 
				"                                    <a role=\"button\" style=\"font-size: 14px; font-weight: bold;\">历史记录</a>\n" + 
				"                                    <span class=\"fa fa-chevron-right\" style=\"width: 15px;\"></span>\n" + 
				"                                </h4>\n" + 
				"                            </div>\n" + 
				"                            <div id=\"softver_collapse_history\" class=\"panel-collapse collapse in\" role=\"tabpanel\" aria-labelledby=\"heading-history\" style=\"height: auto; padding-left: 0px;\">\n" + 
				"                                <div class=\"portlet-body\" style=\"margin-top: 0px; margin-left: 20px; margin-right: 20px;\">\n" + 
				"                                    <div class=\"div_history_table\">\n" + 
				"                                        <ol id=\"softver_details_history\" reversed=\"\"><li style=\"list-style-type:decimal\"><span class=\"his-comment\">fangwencheng&nbsp;&nbsp;2018-08-31 09:04:29&nbsp;&nbsp;创建提测单</span>&nbsp;&nbsp;表单记录:<br>【标题】湖北-V3.9.0.3-胡建<br>【版本号】V3.9.0.3<br>【测试包存放位置】我噶发<br>【建议测试内容】我无法</li></ol>\n" + 
				"                                    </div>\n" + 
				"                                </div>\n" + 
				"                            </div>\n" + 
				"                        </div>\n" + 
				"                    </div>\n" + 
				"                    <div id=\"softver_details_rls_info\" class=\"tab-pane fade hidden\" style=\"position: relative;\" hidden=\"hidden\">\n" + 
				"                       <table class=\"details-table\" style=\"width: 100%;\">\n" + 
				"                            <thead>\n" + 
				"                                <tr>\n" + 
				"                                    <th style=\"width: 25%;\"></th>\n" + 
				"                                    <th style=\"width: 20%;\"></th>\n" + 
				"                                    <th style=\"width: 20%;\"></th>\n" + 
				"                                    <th style=\"width: 35%;\"></th>\n" + 
				"                                </tr>\n" + 
				"                            </thead>\n" + 
				"                            <tbody>\n" + 
				"                                <!-- 这边的标签页也加一个版本号和状态，利于查看 -->\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">版本号：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_rlsnumber\" colspan=\"3\">V3.9.0.3</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">状态：</td>\n" + 
				"                                    <td class=\"table-value\" id=\"softver_details_rlsst\" colspan=\"3\">提测评审中</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">版本发布时间：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_rtime\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">发布版本类型：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_rlstype\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">发布程序类型：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_rlsprogtype\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">是否PDM发行：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_pdm\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">待发布程序存放地址：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_prlsurl\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">待发布程序附件：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\">\n" + 
				"                                        <ul id=\"softver_details_prlsurlfile\"></ul>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">发行说明：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_rlsnote\"></td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">发布变更次数：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_rlschange\">0</td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td class=\"table-key\">发布程序存放地址：</td>\n" + 
				"                                    <td colspan=\"3\" class=\"table-value\" id=\"softver_details_rlsurl\"></td>\n" + 
				"                                </tr>\n" + 
				"                            </tbody>\n" + 
				"                        </table>\n" + 
				"                    </div>\n" + 
				"                </div>\n" + 
				"            </div>\n" + 
				"        </div>\n" + 
				"    </div>\n" + 
				"</div>";
		String copyStr = str;
		str = escapeHtml(str.replace("\n", " "));
		str = str.replaceAll("[\\n][\\s]*[\\n]", "\n").replaceAll("[ \t]{3,}", "  ");
//		str = str.replaceAll("[\\s]", "");
		System.out.println(str);
		
		String tagReplace = tagReplace(copyStr, "table", (fullTag, headTag) -> {
			return "TABLE DELETED";
		});
		System.out.println();
		System.out.println("replace ====> ");
		System.out.println();
		System.out.println(tagReplace);
	}

}
