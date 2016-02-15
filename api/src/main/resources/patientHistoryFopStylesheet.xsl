<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	<xsl:template match="report">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="200cm" margin-top="2cm"
					margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="simpleA4">
				<fo:flow flow-name="xsl-region-body">
					<fo:block font-size="10pt">
						<fo:table table-layout="fixed" width="100%">
							<fo:table-body>
								<xsl:apply-templates />
							</fo:table-body>
						</fo:table>
					</fo:block>
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
	
	
	<xsl:template match="report/dataset[@name='encounters']">
		<xsl:for-each select="rows/row">
			<fo:table-row>
				<xsl:for-each select="./*">
					<fo:table-cell border="solid" border-width="1pt">
						<fo:block>
							<xsl:value-of select="." />
						</fo:block>
					</fo:table-cell>
				</xsl:for-each>
			</fo:table-row>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>