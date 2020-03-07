<head>

	<title>@project.fullname@</title>
	
	<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %> 
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
	<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
    <%! String posInt4 = "^0*([1-9]\\d{0,8}|1\\d{9}|2(0\\d{8}|1([0-3]\\d{7}|4([0-6]\\d{6}|7([0-3]\\d{5}|4([0-7]\\d{4}|8([0-2]\\d{3}|3([0-5]\\d{2}|6([0-3]\\d|4[0-7])))))))))$"; %>
    
	<script type="text/javascript" src="@html.base.js@"></script>
	<script type="text/javascript" src="resources/js/index.js"></script>
    <link href="resources/css/index.css" rel="stylesheet" type="text/css">
	<link href="@html.base.css@" rel="stylesheet" type="text/css">
	<link href="@html.screen.css@" rel="stylesheet" type="text/css">
	<link href="@html.common.css@" rel="stylesheet" type="text/css">
	
	<div id="appBreadcrumbs">
		<ul>
		    <li><a href="#afterBreadcrumbs" class="skipBreadcrumb">Skip Navigation Links<br /></a></li>
		    <li><a href=<%= System.getProperty("fstrf_portal_url") %>>${projectName} Home</a></li>
		    <li>&gt;</li>
		    <li><a href="@context.root@/index">@project.fullname@</a></li>
		</ul>
		<span class="skipTarget"><a id="afterBreadcrumbs"></a></span>
	</div>
	
	<h1 title="@project.fullname@ @project.version@">@project.fullname@</h1>
</head>
<body>

