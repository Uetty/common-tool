<#--report 富文本信息处理--><#macro render richItems htmlCode="">    <#if richItems?? && richItems?size gt 0>        <#list richItems as item>            <#if item.type == 'image'>                <w:p>                    <w:pPr>                        <w:rPr>                            <w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:cs="宋体" w:hint="eastAsia"/>                            <w:lang w:val="en-US" w:eastAsia="zh-CN"/>                        </w:rPr>                    </w:pPr>                    <w:r>                        <w:rPr>                            <w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:cs="宋体" w:hint="eastAsia"/>                            <w:lang w:val="en-US" w:eastAsia="zh-CN"/>                        </w:rPr>                        <w:pict>                            <v:shape id="${item.sharpId}" o:spt="75" alt="${item.name}" type="#_x0000_t75" style="width:${item.width}pt;height:${item.height}pt;" filled="f" o:bordertopcolor="red" o:borderleftcolor="red" o:borderbottomcolor="red" o:borderrightcolor="red" o:preferrelative="t" stroked="t" coordsize="21600,21600">                                <v:path/>                                <v:fill on="f" focussize="0,0"/>                                <v:stroke color="#FF0000"/>                                <v:imagedata r:id="${item.relationId}" o:title=""/>                                <o:lock v:ext="edit" aspectratio="t"/>                                <w10:bordertop type="single" width="18"/>                                <w10:borderleft type="single" width="18"/>                                <w10:borderbottom type="single" width="18"/>                                <w10:borderright type="single" width="18"/>                                <w10:anchorlock/>                            </v:shape>                        </w:pict>                    </w:r>                </w:p>            <#else>                <w:p>                    <w:pPr>                        <w:wordWrap w:val="0"/>                        <w:rPr>                            <w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:cs="宋体" w:hint="eastAsia"/>                            <w:lang w:val="en-US" w:eastAsia="zh-CN"/>                        </w:rPr>                    </w:pPr>                    <w:r>                        <w:rPr>                            <w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:cs="宋体" w:hint="eastAsia"/>                            <w:lang w:val="en-US" w:eastAsia="zh-CN"/>                        </w:rPr>                        <w:t>${item.text?html}</w:t>                    </w:r>                </w:p>            </#if>        </#list>    <#else>        <w:p>            <w:r>                <w:rPr>                    <w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:cs="宋体" w:hint="eastAsia"/>                    <w:lang w:val="en-US" w:eastAsia="zh-CN"/>                </w:rPr>                <w:t><#if htmlCode??>${htmlCode?html}<#else></#if></w:t>            </w:r>        </w:p>    </#if></#macro>