
<#macro addTable tableVo>
            <w:tbl>
                <w:tblPr>
                    <w:tblW w:w="9880" w:type="dxa"/>
                    <w:tblInd w:w="0" w:type="dxa"/>
                    <w:tblBorders>
                        <w:top w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        <w:left w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        <w:bottom w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        <w:right w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        <w:insideH w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        <w:insideV w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                    </w:tblBorders>
                    <w:tblLayout w:type="Fixed"/>
                    <w:tblCellMar>
                        <w:top w:w="0" w:type="dxa"/>
                        <w:left w:w="108" w:type="dxa"/>
                        <w:bottom w:w="0" w:type="dxa"/>
                        <w:right w:w="108" w:type="dxa"/>
                    </w:tblCellMar>
                </w:tblPr>
                <#assign eachWidth = (9880 / tableVo.headColSize)?floor />
                <w:tblGrid>
                    <#list 1..tableVo.headColSize as i>
                    <w:gridCol w:w="${eachWidth}"/>
                    </#list>
                </w:tblGrid>
                <#if (tableVo.headCells)?? && (tableVo.headCells?size > 0)>
                <#list tableVo.headCells as headRow>
                <w:tr>
                    <w:tblPrEx>
                        <w:tblBorders>
                            <w:top w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:left w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:bottom w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:right w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:insideH w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:insideV w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        </w:tblBorders>
                        <w:tblCellMar>
                            <w:top w:w="0" w:type="dxa"/>
                            <w:left w:w="108" w:type="dxa"/>
                            <w:bottom w:w="0" w:type="dxa"/>
                            <w:right w:w="108" w:type="dxa"/>
                        </w:tblCellMar>
                    </w:tblPrEx>
                    <w:trPr/>
                    <#list headRow as cellItem>
                    <#if !(cellItem.colMerge) || (cellItem.colMergeIndex == 0)>
                    <w:tc>
                        <w:tcPr>
                            <w:tcW w:w="${eachWidth}" w:type="dxa"/>
                            <#if cellItem.colMerge>
                            <w:gridSpan w:val="${cellItem.colMergeSize}"/>
                            </#if>
                            <#if cellItem.rowMerge>
                                <#if cellItem.rowMergeIndex == 0>
                            <w:vmerge w:val="restart"/>
                                <#else>
                            <w:vmerge w:val="continue"/>
                                </#if>
                            </#if>
                            <w:shd w:val="clear" w:color="auto" w:fill="D9D9D9"/>
                            <w:vAlign w:val="center"/>
                        </w:tcPr>
                        <w:p>
                            <w:pPr>
                                <w:widowControl/>
                                <w:spacing w:after="0" w:line="240" w:line-rule="auto"/>
                                <w:jc w:val="center"/>
                                <w:rPr>
                                    <w:rFonts w:ascii="微软雅黑" w:h-ansi="微软雅黑" w:fareast="黑体" w:cs="宋体" w:hint="default"/>
                                    <w:color w:val="000000"/>
                                    <w:sz w:val="20"/>
                                    <w:sz-cs w:val="20"/>
                                </w:rPr>
                            </w:pPr>
                            <w:r>
                                <w:rPr>
                                    <w:rFonts w:ascii="微软雅黑" w:h-ansi="微软雅黑" w:fareast="黑体" w:cs="宋体" w:hint="fareast"/>
                                    <w:color w:val="000000"/>
                                    <w:sz w:val="20"/>
                                    <w:sz-cs w:val="20"/>
                                </w:rPr>
                                <w:t>${(cellItem.cellValue)?if_exists?xml}</w:t>
                            </w:r>
                        </w:p>
                    </w:tc>
                    </#if>
                    </#list>
                </w:tr>
                </#list>
                </#if>
                <#if (tableVo.bodyCells)?? && (tableVo.bodyCells?size > 0)>
                <#list tableVo.bodyCells as bodyRow>
                <w:tr>
                    <w:tblPrEx>
                        <w:tblBorders>
                            <w:top w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:left w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:bottom w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:right w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:insideH w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:insideV w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        </w:tblBorders>
                        <w:tblCellMar>
                            <w:top w:w="0" w:type="dxa"/>
                            <w:left w:w="108" w:type="dxa"/>
                            <w:bottom w:w="0" w:type="dxa"/>
                            <w:right w:w="108" w:type="dxa"/>
                        </w:tblCellMar>
                    </w:tblPrEx>
                    <w:trPr/>
                    <#list bodyRow as cellItem>
                    <#if !(cellItem.colMerge) || (cellItem.colMergeIndex == 0)>
                    <w:tc>
                        <w:tcPr>
                            <w:tcW w:w="${eachWidth}" w:type="dxa"/>
                            <#if cellItem.colMerge>
                            <w:gridSpan w:val="${cellItem.colMergeSize}"/>
                            </#if>
                            <#if cellItem.rowMerge>
                                <#if cellItem.rowMergeIndex == 0>
                            <w:vmerge w:val="restart"/>
                                <#else>
                            <w:vmerge w:val="continue"/>
                                </#if>
                            </#if>
                            <w:shd w:val="clear" w:color="auto" w:fill="auto"/>
                        </w:tcPr>
                        <w:p>
                            <w:pPr>
                                <w:spacing w:after="0" w:line="240" w:line-rule="auto"/>
                                <w:rPr>
                                    <w:rFonts w:ascii="微软雅黑" w:h-ansi="微软雅黑" w:fareast="黑体" w:cs="宋体" w:hint="default"/>
                                    <w:color w:val="000000"/>
                                    <w:sz w:val="20"/>
                                    <w:sz-cs w:val="20"/>
                                </w:rPr>
                            </w:pPr>
                            <w:r wsp:rsidRPr="00406CA1">
                                <w:rPr>
                                    <w:rFonts w:ascii="微软雅黑" w:fareast="黑体" w:h-ansi="微软雅黑" w:cs="宋体" w:hint="fareast"/>
                                    <wx:font wx:val="黑体"/>
                                    <w:color w:val="000000"/>
                                    <w:sz w:val="20"/>
                                    <w:sz-cs w:val="20"/>
                                </w:rPr>
                                <w:t>${(cellItem.cellValue)?if_exists?xml}</w:t>
                            </w:r>
                        </w:p>
                    </w:tc>
                    </#if>
                    </#list>
                </w:tr>
                </#list>
                <#else>
                <w:tr>
                    <w:tblPrEx>
                        <w:tblBorders>
                            <w:top w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:left w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:bottom w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:right w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:insideH w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                            <w:insideV w:val="single" w:sz="4" wx:bdrwidth="10" w:space="0" w:color="auto"/>
                        </w:tblBorders>
                        <w:tblCellMar>
                            <w:top w:w="0" w:type="dxa"/>
                            <w:left w:w="108" w:type="dxa"/>
                            <w:bottom w:w="0" w:type="dxa"/>
                            <w:right w:w="108" w:type="dxa"/>
                        </w:tblCellMar>
                    </w:tblPrEx>
                    <w:trPr/>
                    <#list 1..tableVo.headColSize as i>
                    <w:tc>
                        <w:tcPr>
                            <w:tcW w:w="${eachWidth}" w:type="dxa"/>
                            <w:shd w:val="clear" w:color="auto" w:fill="auto"/>
                        </w:tcPr>
                        <w:p>
                            <w:pPr>
                                <w:spacing w:after="0" w:line="240" w:line-rule="auto"/>
                                <w:rPr>
                                    <w:rFonts w:ascii="微软雅黑" w:h-ansi="微软雅黑" w:fareast="黑体" w:cs="宋体" w:hint="default"/>
                                    <w:color w:val="000000"/>
                                    <w:sz w:val="20"/>
                                    <w:sz-cs w:val="20"/>
                                </w:rPr>
                            </w:pPr>
                            <w:r wsp:rsidRPr="00406CA1">
                                <w:rPr>
                                    <w:rFonts w:ascii="微软雅黑" w:fareast="黑体" w:h-ansi="微软雅黑" w:cs="宋体" w:hint="fareast"/>
                                    <wx:font wx:val="黑体"/>
                                    <w:color w:val="000000"/>
                                    <w:sz w:val="20"/>
                                    <w:sz-cs w:val="20"/>
                                </w:rPr>
                                <w:t></w:t>
                            </w:r>
                        </w:p>
                    </w:tc>
                    </#list>
                </w:tr>
                </#if>
            </w:tbl>
</#macro>

