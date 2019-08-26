<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:param name="numColumns">4</xsl:param>
  
	<xsl:template match="patientHistory">
	    <fo:root>
	    	<fo:layout-master-set>
	        	<fo:simple-page-master master-name="A4-portrait" page-height="29.7cm" page-width="21.0cm" margin="2cm">
					<fo:region-body/>
				</fo:simple-page-master>
			</fo:layout-master-set>
	      
			<fo:page-sequence master-reference="A4-portrait">
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="visit"/>
				</fo:flow>
			</fo:page-sequence>
	    </fo:root>
	</xsl:template>
	
	<xsl:template match="report/dataset[@name='demographics']">
		<xsl:for-each select="rows/row">
			<xsl:for-each select="./*">
				<fo:table-row>
					<fo:table-cell>
						<fo:block>
							<xsl:value-of select="name(.)" />
							:
							<xsl:value-of select="." />
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</xsl:for-each>
		</xsl:for-each>
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
			<xsl:choose>
				<xsl:when test="count(obs[@type!='Text']) &gt; 0">
					<fo:table>
						<xsl:call-template name="repeat-fo-table-column">
						    <xsl:with-param name="count" select="$numColumns" />
					    </xsl:call-template>
						
				        <fo:table-body>
							<!-- We select the first obs and and those that match for a row start -->
				            <xsl:apply-templates select="obs[(position() = 1 or (position() mod $numColumns) = 1)]" mode="row"/>
				    	</fo:table-body>
					</fo:table> 
		 		</xsl:when>
		 		<xsl:otherwise>
		 			<xsl:apply-templates select="obs" mode="regular"/>
		 		</xsl:otherwise>
	 		</xsl:choose>
		</fo:block>
	</xsl:template>
	
	<!-- Display of obs. not in table-->
	<xsl:template match="obs" mode="regular">
		<fo:block margin="2mm">
			<fo:block font-size="10">
				<fo:block font-size="8" font-style="italic" margin-bottom="1mm" margin-right="3mm">
					<fo:block><xsl:value-of select="@label" /></fo:block>
<!-- 					<fo:block><xsl:value-of select="@time" /></fo:block> -->
				</fo:block>
				<xsl:value-of select="." />
			</fo:block>
		</fo:block>
	</xsl:template>
	
	<!-- In-table obs.: selecting each row's elements -->
	<xsl:template match="obs" mode="row">
		<fo:table-row>
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