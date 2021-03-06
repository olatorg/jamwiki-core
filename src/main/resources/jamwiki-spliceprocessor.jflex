/*
 * This class provides the capability to slice and splice an article to
 * insert or remove a section of text.  In this case a "section" is
 * defined as a body of text between two heading tags of the same level,
 * such as two &lt;h2&gt; tags.
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiSpliceProcessor
%extends AbstractLexer
%type String
%unicode
%ignorecase

/* code included in the constructor */
%init{
    allowHtml = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiSpliceProcessor.class.getName());
    protected boolean allowHtml = false;
    protected int section = 0;
    protected int sectionDepth = 0;
    protected int targetSection = 0;
    protected String replacementText = null;
    protected boolean inTargetSection = false;
    
    /**
     *
     */
    protected String processHeading(int level, String headingText) {
        this.section++;
        if (inTargetSection && this.sectionDepth >= level) {
            inTargetSection = false;
        } else if (this.targetSection == this.section) {
            WikiHeadingTag parserTag = new WikiHeadingTag();
            this.parseToken(headingText, parserTag);
            inTargetSection = true;
            this.sectionDepth = level;
            if (this.mode == JFlexParser.MODE_SPLICE) return this.replacementText;
        }
        return returnText(headingText);
    }
    
    /**
     *
     */
    private String returnText(String text) {
        return ((inTargetSection && this.mode == JFlexParser.MODE_SPLICE) || (!inTargetSection && this.mode == JFlexParser.MODE_SLICE)) ? "" : text;
    }
    
    /**
     *
     */
    public void init(ParserInput parserInput, ParserDocument parserDocument, int mode) throws Exception {
        this.parserInput = parserInput;
        this.parserDocument = parserDocument;
        this.mode = mode;
        // validate parser settings
        boolean validated = true;
        if (this.mode != JFlexParser.MODE_SPLICE && this.mode != JFlexParser.MODE_SLICE) validated = false;
        if (this.parserInput.getTopicName() == null) validated = false;
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
    
    /**
     *
     */
    public void setReplacementText(String replacementText) {
        // replacementText must end with a newline, otherwise sections get spliced together
        if (replacementText == null) return;
        if (!replacementText.endsWith("\n") && !replacementText.endsWith("\r")) {
            replacementText += "\r\n";
        }
        this.replacementText = replacementText;
    }
    
    /**
     *
     */
    public void setTargetSection(int targetSection) {
        this.targetSection = targetSection;
    }
%}

/* character expressions */
newline            = \r|\n|\r\n
whitespace         = {newline} | [ \t\f]

/* non-container expressions */
h1                 = "=" [^=\n]+ ~"="
h2                 = "==" [^=\n]+ ~"=="
h3                 = "===" [^=\n]+ ~"==="
h4                 = "====" [^=\n]+ ~"===="
h5                 = "=====" [^=\n]+ ~"====="

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)

/* comments */
htmlcomment        = "<!--" ~"-->"

%state NORMAL, PRE

%%

/* ----- parsing tags ----- */

<PRE, NORMAL>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    WikiNowikiTag parserTag = new WikiNowikiTag();
    return returnText(this.parseToken(yytext(), parserTag));
}

/* ----- nowiki ----- */

<NORMAL>{htmlprestart} {
    if (allowHtml) {
        beginState(PRE);
    }
    HtmlPreTag parserTag = new HtmlPreTag();
    return returnText(this.parseToken(yytext(), parserTag));
}

<PRE>{htmlpreend} {
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    HtmlPreTag parserTag = new HtmlPreTag();
    return returnText(this.parseToken(yytext(), parserTag));
}

/* ----- comments ----- */

<NORMAL>{htmlcomment} {
    HtmlCommentTag parserTag = new HtmlCommentTag();
    return returnText(this.parseToken(yytext(), parserTag));
}

/* ----- headings ----- */

<NORMAL>^{h1} {
    return processHeading(1, yytext());
}

<NORMAL>^{h2} {
    return processHeading(2, yytext());
}

<NORMAL>^{h3} {
    return processHeading(3, yytext());
}

<NORMAL>^{h4} {
    return processHeading(4, yytext());
}

<NORMAL>^{h5} {
    return processHeading(5, yytext());
}

/* ----- default ----- */

<PRE, NORMAL>{whitespace} {
    CharacterTag parserTag = new CharacterTag();
    return returnText(this.parseToken(yytext(), parserTag));
}

<PRE, NORMAL>. {
    CharacterTag parserTag = new CharacterTag();
    return returnText(this.parseToken(yytext(), parserTag));
}
