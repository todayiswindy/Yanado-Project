package com.yanado.controller.auc;

import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yanado.controller.user.UserSessionUtils;
import com.yanado.dto.Auc;
import com.yanado.service.AucService;

@Controller
@SessionAttributes("auc")
public class CreateAucController {
	   @Autowired
	   private AucService aucService;
	   
	   @ModelAttribute("auc")
	   public Auc formBacking(HttpServletRequest request) {
	      Auc auc = new Auc();
		  UserSessionUtils uSession = new UserSessionUtils();
		  @SuppressWarnings("static-access")
		  String userId = uSession.getLoginUserId(request.getSession());
	      //userId = "admin";
			/*
			 * auc.setuserId(userId); auc.setstatus(1);
			 */

	      return auc;
	   }
	   
	   @RequestMapping(value="auc/create",method=RequestMethod.GET)
	   public String from() {
	      return "auc/form";
	   }
	   
	   @RequestMapping(value = "auc/create", method = RequestMethod.POST)
	   public String createShopping(@Valid @ModelAttribute("auc") Auc auc, BindingResult result,
			   RedirectAttributes red, SessionStatus status) {
		   
	      if (result.hasErrors()) {
	    	 System.out.println(result.getAllErrors());
	         return "auc/form";
	      }

		  status.setComplete();

	      Date date = new Date(System.currentTimeMillis());
	      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	      String reg = formatter.format(date);
	      /*String start = formatter.format(auc.getstartDate());
	      
	      if (reg.equals(start)) {
	    	  auc.setstatus(2);
	      }
	      
	      aucService.createAuc(auc);
	      red.addAttribute("aucNo", auc.getaucNo());*/
	      return "redirect:/auc/read";
	   }

}
