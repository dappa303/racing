<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:template match="/">
        <xsl:element name="speedmaps">
            <xsl:attribute name="date">
                <xsl:value-of select="/speedmaps/@date"/>
            </xsl:attribute>
            <xsl:attribute name="track">
                <xsl:value-of select="/speedmaps/@track"/>
            </xsl:attribute>

            <xsl:apply-templates select="/speedmaps/fields/race"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="/speedmaps/fields/race">
        <xsl:element name="race">
            <xsl:attribute name="number">
                <xsl:value-of select="./@number"/>
            </xsl:attribute>
            <xsl:attribute name="distance">
                <xsl:value-of select="./@distance"/>
            </xsl:attribute>
            
            <xsl:attribute name="pace">
                <xsl:value-of select="/speedmaps/positions/race[@number=current()/@number]/@pace"/>
            </xsl:attribute>
            
            
            <xsl:apply-templates select="horse">
                <xsl:with-param name="racenum" select="./@number"/>
                <xsl:sort select="@scratched"/>
                <xsl:sort select="@barrier" data-type="number"/>
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <xsl:template match="horse">
        <xsl:param name = "racenum" />
        <xsl:element name="horse">
            <xsl:attribute name="number">
                <xsl:value-of select="./@number"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="./@name"/>
            </xsl:attribute>
            <xsl:attribute name="barrier">
                <xsl:value-of select="./@barrier"/>
            </xsl:attribute>
            <xsl:if test="./@scratched='false'">
                <xsl:variable name="fwd" select="/speedmaps/positions/race[@number=$racenum]/horse[@name=current()/@name]/@forward"/>
                <xsl:variable name="wd" select="/speedmaps/positions/race[@number=$racenum]/horse[@name=current()/@name]/@wide"/>
                <xsl:attribute name="effective-barrier">
                    <xsl:value-of select="position()"/>
                </xsl:attribute>
                <xsl:attribute name="forward">
                    <xsl:value-of select="$fwd"/>
                </xsl:attribute>
                <xsl:attribute name="wide">
                    <xsl:value-of select="$wd"/>
                </xsl:attribute>
                <xsl:attribute name="forward-saved">
                    <xsl:value-of select="$fwd"/>
                </xsl:attribute>
                <xsl:attribute name="wide-saved">
                    <xsl:value-of select="$wd"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="scratched">
                <xsl:value-of select="./@scratched"/>
            </xsl:attribute>
        </xsl:element>
        
    </xsl:template>
</xsl:stylesheet>
