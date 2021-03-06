<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>About Us : GeneDB - GeneDB</title>

    <script type="text/javascript" src="http://js.sanger.ac.uk/urchin.js"></script>

    <link rel="stylesheet" href="/includes/style/genedb/main.css" type="text/css" />
    <script type="text/javascript" src="/includes/scripts/jquery/jquery-genePage-combined.js"></script>

    <script type="text/javascript">
        $(function(){
          $("#nav > li")
            .mouseover(function(){$(this).addClass("over");})
            .mouseout (function(){$(this).removeClass("over");});
        });
    </script>


</head>
<body>
<div id="container">

<div id="header">
<a href="/Homepage"><img src="/includes/image/GeneDB-logo.png" border="0" alt="GeneDB" class="float-left-and-offset" id="logo" /></a>

<div class="float-right" >
<div class="baby-blue-top"></div>
<div id="search" class="baby-blue">
<form action="/QuickSearchQuery" method="get">
<table cellpadding="0" cellspacing="0" width="100%" class="search-table">
<tr>
<td>
<input type="hidden" name="pseudogenes" value="true" />
<input type="hidden" name="product" value="true" />
<input type="hidden" name="allNames" value="true" />
<input type="text" name="searchText" class="search-box" /></td><td align="right"><input type="image" src="/includes/image/button-search.gif" /></td>
</tr>
<tr>
<td colspan="2">

<select name="taxons">
<option  class="Level0DropDown" value="Root">&nbsp;&nbsp;All Organisms</option>
<option  class="Level1DropDown" value="Helminths">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Helminths</option>
<option  class="Level2DropDown" value="Platyhelminths">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Platyhelminths</option>
<option  class="Level3DropDown" value="Sjaponicum">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S. japonicum</option>

<option  class="Level3DropDown" value="Smansoni">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S. mansoni</option>
<option  class="Level1DropDown" value="Protozoa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Protozoa</option>
<option  class="Level2DropDown" value="Apicomplexa">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Apicomplexa</option>
<option  class="Level3DropDown" value="Etenella">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;E. tenella</option>
<option  class="Level3DropDown" value="Ncaninum">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;N. caninum</option>
<option  class="Level3DropDown" value="Plasmodium">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Plasmodium</option>
<option  class="Level4DropDown" value="Pchabaudi">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;P. chabaudi</option>
<option  class="Level4DropDown" value="Pfalciparum">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;P. falciparum</option>
<option  class="Level4DropDown" value="Pknowlesi">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;P. knowlesi</option>

<option  class="Level4DropDown" value="Pvivax">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;P. vivax</option>
<option  class="Level4DropDown" value="Pyoelii">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;P. yoelii</option>
<option  class="Level2DropDown" value="Kinetoplastida">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Kinetoplastida</option>
<option  class="Level3DropDown" value="Leishmania">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Leishmania</option>
<option  class="Level4DropDown" value="Lbraziliensis">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;L. braziliensis</option>
<option  class="Level4DropDown" value="Linfantum">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;L. infantum</option>
<option  class="Level4DropDown" value="Lmajor">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;L. major</option>
<option  class="Level4DropDown" value="Lmexicana">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;L. mexicana</option>
<option  class="Level3DropDown" value="Trypanosoma">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Trypanosoma</option>

<option  class="Level4DropDown" value="Tbruceibrucei427">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;T. brucei brucei 427</option>
<option  class="Level4DropDown" value="Tbruceibrucei927">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;T. brucei brucei 927</option>
<option  class="Level4DropDown" value="Tbruceigambiense">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;T. bruceigambiense</option>
<option  class="Level4DropDown" value="Tcongolense">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;T. congolense</option>
<option  class="Level4DropDown" value="Tcruzi">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;T. cruzi</option>
<option  class="Level4DropDown" value="Tvivax">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;T. vivax</option>
</select>
</td>
</tr>
</table>
</form>

</div>
<div class="baby-blue-bot"></div>
</div><!-- end float right of search box -->
<br class="clear" />
</div><!-- end header block -->

<br class="clear" />

<div id="navigation">
<ul id="nav">
<li><a href="/Homepage">Home</a>
</li>

<li><a href="/Page/aboutUs">About us</a></li>
<li class="has-sub"><a href="/Query">Searches</a>

<ul class="sub-menu">
<li><a href="/Query/geneType?taxons=" >By Gene Type</a></li>
<li><a href="/Query/geneLocation?taxons=" >By Location</a></li>
<li><a href="/Query/proteinLength?taxons=" >By Protein Length</a></li>
<li><a href="/Query/proteinMass?taxons=">By Molecular Mass</a></li>
<li><a href="/Query/proteinNumTM?taxons=">By No. TM domains</a></li>
<li><a href="/Query/proteinTargetingSeq?taxons=">By Targeting Seqs.</a></li>
<li><a href="/Query/simpleName?taxons=">Gene names</a></li>
<li><a href="/Query/product?taxons=">Product</a></li>

<li><a href="/Query/go?taxons=">GO term/id</a></li>
<li><a href="/Query/ec?taxons=">EC number</a></li>
<li><a href="/Query/pfam?taxons=">Pfam ID or keyword</a></li>
</ul>
<!-- end sub menu -->
</li>
<li class="has-sub"><a href="">Browse</a>
<ul class="sub-menu">
<li><a href="/category/genedb_products?taxons=">Products</a></li>
<li><a href="/category/ControlledCuration?taxons=">Controlled Curation</a></li>

<li><a href="/category/biological_process?taxons=">Biological Process</a></li>
<li><a href="/category/cellular_component?taxons=">Cellular Component</a></li>
<li><a href="/category/molecular_function?taxons=">Molecular Function</a></li>
</ul>
</li>
<!-- end sub menu -->
</ul>
</div><!-- end navigation block -->


<br>

<div id="col-2-1">

<br />


<div class="readableText">
<h2>Problems...</h2>

<p>Sorry, we've encountered an internal problem. </p>

</div>

</div>
<br class="clear" />

<div id="footer">
<div class="footer-top"></div>
<div class="light-grey">
<table cellpadding="0" cellspacing="0" width="100%">
<tr>
<td valign="top" align="left">
<p>&copy; 2009-2016 and hosted by the <a href="http://www.sanger.ac.uk/">Sanger Institute</a></p>
</td>

<td valign="top" align="right">
<p>Comments/Questions: <a href="mailto:webmaster@genedb.org">Email us</a>
</td>
</tr>
</table>
</div>
<div class="footer-bot"></div>
</div><!-- end footer -->
</div><!-- end container -->

<script type="text/javascript">
_userv=0;
urchinTracker();
<script type="text/javascript" src="/zxtm/piwik2.js"></script>
</script>
</body>
</html>
