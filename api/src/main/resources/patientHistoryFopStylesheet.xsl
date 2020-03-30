<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:param name="numColumns">4</xsl:param>
  
		<xsl:template match="patientHistory">
	    <fo:root>
	    	<fo:layout-master-set>
	        	<fo:simple-page-master master-name="A4-portrait" page-height="29.7cm" page-width="21.0cm" margin-top="1cm" margin-bottom="1cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body region-name="xsl-region-body" margin-top="2cm" margin-bottom="1cm"/>
					<fo:region-before region-name="xsl-region-header" extent="1cm"/>
					<!-- <fo:region-after region-name="xsl-region-footer" extent="1cm"/> -->
				</fo:simple-page-master>
			</fo:layout-master-set>
			<xsl:variable name="logoImage"><xsl:value-of select="header/branding/logo"/></xsl:variable>
			
			<fo:page-sequence master-reference="A4-portrait" initial-page-number="1">
				<xsl:choose>
					<xsl:when test="header">
						<fo:static-content flow-name="xsl-region-header">
							<fo:table>
								<fo:table-column/>
								<fo:table-column/>
								<fo:table-body>
									<fo:table-row>
										<fo:table-cell>
											<fo:block>
												<fo:external-graphic height="2cm" content-height="scale-to-fit" src="{$logoImage}"/>
											</fo:block>
										</fo:table-cell>
										<fo:table-cell>
											<fo:block text-align="right">
												<fo:inline font-size="8" vertical-align="top">
													<xsl:value-of select="header/headerText"/>
												</fo:inline>
											</fo:block>
											<fo:block text-align="right">
												<fo:inline font-size="8">
													<xsl:value-of select="i18n/pageString"/>&#160;<fo:page-number/>&#160;<xsl:value-of select="i18n/ofString"/>&#160;<fo:page-number-citation-last ref-id="last-page"/>
												</fo:inline>
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
								</fo:table-body>
							</fo:table>
						</fo:static-content>
					</xsl:when>
					<xsl:otherwise>
					</xsl:otherwise>
				</xsl:choose>
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="demographics"/>
					<xsl:apply-templates select="visit"/>
					<fo:block id="last-page"/>
				</fo:flow>
<!--			<fo:static-content flow-name="xsl-region-footer">
					<fo:block><xsl:value-of select="@footer" /></fo:block>
				</fo:static-content>
-->
			</fo:page-sequence>
	    </fo:root>
	</xsl:template>
	
	<xsl:template match="demographics">
		<fo:block  margin-bottom="10mm">
			<fo:table>
				<xsl:call-template name="repeat-fo-table-column">
				    <xsl:with-param name="count" select="$numColumns" />
			    </xsl:call-template>
			    <fo:table-body>
					<xsl:apply-templates select="demographic[(position() = 1 or (position() mod $numColumns) = 1)]" mode="row" />
		    	</fo:table-body>
			</fo:table> 
		</fo:block>
	</xsl:template>
	
	<xsl:template match="demographic" mode="row">
		<fo:table-row page-break-inside="avoid">
	        <!-- We select the current obs and add the following ones whose position doesn't exceed the row's width -->
	        <xsl:apply-templates select=".|following-sibling::demographic[position() &lt; ($numColumns + 0)]" mode="cell"/>
        </fo:table-row>
    </xsl:template>
    
    <xsl:template match="demographic" mode="cell">
        <fo:table-cell>
			<fo:block margin="2mm">
				<fo:block font-size="13">
					<fo:block font-size="13" font-style="italic" margin-bottom="1mm" margin-right="3mm">
						<fo:block><xsl:value-of select="@label" /></fo:block>
					</fo:block>
					<xsl:value-of select="." />
				</fo:block>
			</fo:block> 
        </fo:table-cell>
    </xsl:template>	
	
	<xsl:template match="visit">
		<fo:block margin-bottom="10mm">
			<fo:block font-size="18"> <xsl:value-of select="@type"/> </fo:block>
			<xsl:apply-templates select="encounter"/>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="encounter">
		<fo:block margin-top="6mm">
			<fo:block font-size="13">
				<xsl:value-of select="@label"/>
			</fo:block>
			<fo:block font-size="9">
				<xsl:value-of select="@provider"/> <xsl:text>&#x9;</xsl:text>-<xsl:text>&#x9;</xsl:text> <xsl:value-of select="@time"/> 
			</fo:block>
			
			<!-- don't start a table, table body etc if there are no obs that will create table rows and cells-->
			<xsl:choose>
				<!-- when some other obs exist like coded  -->
				<xsl:when test="count(obs[@type!='Text' and @type!='Image']) &gt; 0">
				<!-- filter out the obs that shouldnt go in a table while selecting to a new template,
				this allows calling position() and getting the expected 1 and mod 1 -->
					<!-- put text and coded, et c in table -->
					<xsl:apply-templates select="obs[@type!='Image']" mode="table"/>
				</xsl:when>
				<!-- otherwise put text obs on it's own line (e.g. long free text messages, if they're all the encounter has) -->
				<xsl:otherwise>
					<xsl:for-each select="obs[@type='Text']">
				 		<xsl:apply-templates select="obs" mode="regular"/>
				 	</xsl:for-each>
			 	</xsl:otherwise>
			</xsl:choose>
			<!-- after all other obs, add the image obs as external graphics -->
			<xsl:for-each select="obs[@type='Image']">
				<fo:block margin="2mm" page-break-inside="avoid">
 					<fo:block font-size="8" font-style="italic" margin-bottom="1mm" margin-right="3mm">
						<fo:block><xsl:value-of select="@label" /></fo:block>
					</fo:block>
					<xsl:variable name="obsImage"><xsl:value-of select="."/></xsl:variable>
					<fo:external-graphic width="100%" content-width="scale-to-fit" content-height="scale-to-fit" src="{$obsImage}"/>
				</fo:block>
			</xsl:for-each>
		</fo:block>
	</xsl:template>
	
	<!-- Display of obs. not in table-->
	<xsl:template match="obs" mode="regular">
		<fo:block margin="2mm" page-break-inside="avoid">
			<fo:block font-size="10">
				<fo:block font-size="8" font-style="italic" margin-bottom="1mm" margin-right="3mm">
					<fo:block><xsl:value-of select="@label" /></fo:block>
<!-- 					<fo:block><xsl:value-of select="@time" /></fo:block> -->
					</fo:block>
					<xsl:value-of select="." />
				</fo:block>
			</fo:block>
	</xsl:template>
	
	<!-- start a new table -->
	<xsl:template match="obs" mode="table">
		<!-- don't start a new table if the position doesnt match a new row -->
		<xsl:if test="(position() = 1 or (position() mod $numColumns) = 1)">
			<fo:table>
				<!-- add columns -->
				<xsl:call-template name="repeat-fo-table-column">
	    			<xsl:with-param name="count" select="$numColumns" />
	   			</xsl:call-template>
				<!-- add filtered obs (not complex image) to table body -->
				<!-- We select the first obs and and those that match for a row start -->
				<fo:table-body>
					<xsl:apply-templates select="." mode="row"/>
	    	</fo:table-body>
			</fo:table>
		</xsl:if>
	</xsl:template>
	
	<!-- In-table obs.: selecting each row's elements -->
	<xsl:template match="obs" mode="row">
		<fo:table-row page-break-inside="avoid">
		<!-- filter those obs that are handled by other formatting, @type!='Text' and @type!='Image' -->
      <!-- We select the current obs and add the following ones whose position doesn't exceed the row's width -->
      <xsl:apply-templates select=".|following-sibling::obs[position() &lt; ($numColumns + 0)]" mode="cell"/>
    </fo:table-row>
	</xsl:template>	
    
  <!-- In-table obs.: in rows cell -->
  <xsl:template match="obs" mode="cell">
      <fo:table-cell>
				<fo:block margin="2mm">
					<fo:block font-size="10">
						<fo:block font-size="8" font-style="italic" margin-bottom="1mm" margin-right="3mm">
							<fo:block><xsl:value-of select="@label" /></fo:block>
	<!-- 						<fo:block><xsl:value-of select="@time" /></fo:block> -->
						</fo:block>
						<xsl:value-of select="." />
					</fo:block>
				</fo:block> 
      </fo:table-cell>
  </xsl:template>	
	
	<!-- Function to loop as many times as necessary to produce: '<fo:table-column/>' -->
	<xsl:template name="repeat-fo-table-column">
		<xsl:param name="count" />
		<xsl:if test="$count &gt; 0">
			<fo:table-column/>	
		    <xsl:call-template name="repeat-fo-table-column">
			    <xsl:with-param name="count" select="$count - 1" />
		    </xsl:call-template>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>