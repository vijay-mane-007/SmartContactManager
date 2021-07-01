console.log("this is script file")

const toggleSidebar = () => {
 
 if($(".sidebar").is(":visible")){
 	 //ture 
 	// tr band karaych
 	$(".sidebar").css("display","none");
 	$(".content").css("margin-left","0%");
 	
 } else {
 		//false
 		// show karaych
 		$(".sidebar").css("display","block");
 		$(".content").css("margin-left","20%");
 	}
 };