package com.ines.zmx;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public String login(Model model) {
		return "login";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String logincheck(Model model,@RequestParam(value="userid") String userid,
			@RequestParam(value="password") String password) {
		List<Map<String,Object>> list;
		String message = "";
		if(userid == "") {
			message = "ユーザ名を入力してください。<br />";
		}
		else if(userid.length() < 8) {
			message = "ユーザ名は8桁で入力してください。<br />";
		}
		else {
			Pattern p = Pattern.compile("^[0-9a-zA-Z]+$");
	        Matcher m = p.matcher(userid);
			if(!m.find()) {
				message = "ユーザ名は半角英数字で入力してください。<br />";
			}
		}
		
		if(password == "") {
			message += "パスワードを入力してください。<br />";
		}
		
		if(message != "") {
			model.addAttribute("message", message);
			model.addAttribute("userid", userid);
			return "login";
		}
		else {
			list = jdbcTemplate.queryForList("select * from users where userid=?",userid);
			if (list.isEmpty()){
				message = "ユーザ名またはパスワードに誤りがあります。";
			}
			else {
				if(password.equals(list.get(0).get("password"))) {
					return "index";
				}
				else {
					message = "ユーザ名またはパスワードに誤りがあります。";
				}
			}
			model.addAttribute("message", message);
			model.addAttribute("userid", userid);
			return "login";
		}
	}
	
	@RequestMapping(value="/menu", method=RequestMethod.GET)
	public String menu(Model model) {
		return "index";
	}
	
	@RequestMapping(value="/add", method=RequestMethod.GET)
	public String add(Model model) {
		model.addAttribute("projecttypeItems",getSelectedItems("projecttype"));
		model.addAttribute("statusItems",getSelectedItems("status"));
		model.addAttribute("languageItems",getSelectedItems("language"));
		return "add";
	}
	
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public String addcheck(Model model, @RequestParam(value="start_date") String start_date,
										@RequestParam(value="project_no") String project_no,
										@RequestParam(value="project_name") String project_name,
										@RequestParam(value="protype_code") String protype_code,
										@RequestParam(value="language_code") String language_code,
										@RequestParam(value="summary") String summary,
										@RequestParam(value="status_code") String status_code,
										@RequestParam(value="customer") String customer,
										@RequestParam(value="charge") String charge,
										@RequestParam(value="reviewer") String reviewer,
										@RequestParam(value="release_date") String release_date,
										@RequestParam(value="remarks") String remarks) throws ParseException {
		String message = "";
		if(start_date.equals("")) {
			message = "発生日を入力してください。<br />";
		}
		else {
			Pattern p = Pattern.compile("^[0-9]{4}/[0-9]{2}/[0-9]{2}$");
	        Matcher m = p.matcher(start_date);
			if(!m.find()) {
				message = "発生日は yyyy/MM/dd の形式で入力してください。<br />";
			}
			else {
				DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
				try {
				    format.setLenient(false);
				    format.parse(start_date);
				} catch (ParseException e) {
					message = "発生日は カレンダーに存在しない日付です。<br />";
				}
			}
		}
		
		if(project_no.equals("")) {
			message += "案件番号を入力してください。<br />";
		}
		
		if(project_name.equals("")) {
			message += "案件名を入力してください。<br />";
		}
		
		if(protype_code.equals("")) {
			message += "工程区分を選択してください。<br />";
		}
		
		if(language_code.equals("")) {
			message += "開発言語を選択してください。<br />";
		}
		
		if(summary.equals("")) {
			message += "概要を入力してください。<br />";
		}
		
		if(status_code.equals("")) {
			message += "状態を選択してください。<br />";
		}
		
		if(!release_date.equals("")) {
			Pattern p = Pattern.compile("^[0-9]{4}/[0-9]{2}/[0-9]{2}$");
	        Matcher m = p.matcher(release_date);
			if(!m.find()) {
				message += "リリース日は yyyy/MM/dd の形式で入力してください。<br />";
			}
			else {
				DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
				try {
				    format.setLenient(false);
				    format.parse(release_date);
				} catch (ParseException e) {
					message += "リリース日は カレンダーに存在しない日付です。<br />";
				}
			}
		}
		
		if(message.equals("")) {
			List<Map<String,Object>> list;
		    list = jdbcTemplate.queryForList("select * from sequence where name=?", "project_id");
		    if(Long.parseLong(list.get(0).get("number").toString()) >= 10000) {
		    	message = "登録可能な一連番号が無いため、登録を行えません。";
		    }
		    else {
		    	Long project_id = Long.parseLong(list.get(0).get("number").toString());
		    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				Date start_date_date = sdf.parse(start_date);
		    	if(release_date.equals("")) {
		    		jdbcTemplate.update("INSERT INTO project (project_id, start_date, project_no, project_name, "
									   + "protype_code, language_code, summary, status_code, "
									   + "customer, charge, reviewer, remarks) "
									   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", 
									   project_id, start_date_date, project_no, project_name,
									   protype_code, language_code, summary, status_code,
									   customer, charge, reviewer, remarks);
		    	}
		    	else {
					Date release_date_date = sdf.parse(release_date);
			    	jdbcTemplate.update("INSERT INTO project (project_id, start_date, project_no, project_name, "
			    										   + "protype_code, language_code, summary, status_code, "
			    										   + "customer, charge, reviewer, release_date, remarks) "
			    										   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", 
			    										   project_id, start_date_date, project_no, project_name,
			    										   protype_code, language_code, summary, status_code,
			    										   customer, charge, reviewer, release_date_date, remarks);
		    	}
		    	jdbcTemplate.update("UPDATE sequence SET number = ? WHERE name = 'project_id'", project_id + 1);
				model.addAttribute("projecttypeItems",getSelectedItems("projecttype"));
				model.addAttribute("statusItems",getSelectedItems("status"));
				model.addAttribute("languageItems",getSelectedItems("language"));
		    	return "redirect:/add";
		    }
		}
		model.addAttribute("start_date", start_date);
		model.addAttribute("project_no", project_no);
		model.addAttribute("project_name", project_name);
		model.addAttribute("protype_code", protype_code);
		model.addAttribute("language_code", language_code);
		model.addAttribute("summary", summary);
		model.addAttribute("status_code", status_code);
		model.addAttribute("customer", customer);
		model.addAttribute("charge", charge);
		model.addAttribute("reviewer", reviewer);
		model.addAttribute("release_date", release_date);
		model.addAttribute("remarks", remarks);
		model.addAttribute("projecttypeItems",getSelectedItems("projecttype"));
		model.addAttribute("statusItems",getSelectedItems("status"));
		model.addAttribute("languageItems",getSelectedItems("language"));
		model.addAttribute("message", message);
		return "add";
		
	}
	
	private Map<String,String> getSelectedItems(String item){
	     Map<String, String> selectMap = new LinkedHashMap<String, String>();
	     List<Map<String,Object>> list;
	     list = jdbcTemplate.queryForList("select * from " + item);
	     for(int i = 0;i<list.size();i++) {
	    	 if(item.equals("projecttype")){
	    		 selectMap.put(list.get(i).get("protype_code").toString(), list.get(i).get("protype_name").toString());
	    	 }
	    	 else {
	    		 selectMap.put(list.get(i).get(item + "_code").toString(), list.get(i).get(item + "_name").toString());
	    	 }
    	 }
	     return selectMap;
	}	
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(Model model) {
		List<Map<String,Object>> list;
	    list = jdbcTemplate.queryForList("select * from project order by project_id");
	    List<Map<String,Object>> valuelist;
	    Map<String,Object> valuemap;
	    String project_id;
	    String message = "";
	    for (int i = 0;i<list.size();i++) {
	    	valuemap = list.get(i);
	    	valuemap.put("project_id_num", valuemap.get("project_id"));
	    	project_id = String.format("%04d", Long.parseLong(valuemap.get("project_id").toString()));
	    	valuemap.remove("project_id");
	    	valuemap.put("project_id", project_id);
	    	valuelist = jdbcTemplate.queryForList("select * from projecttype where protype_code=?", valuemap.get("protype_code"));
	    	valuemap.remove("protype_code");
	    	valuemap.put("protype_code", valuelist.get(0).get("protype_name"));
	    	valuelist = jdbcTemplate.queryForList("select * from language where language_code=?", valuemap.get("language_code"));
	    	valuemap.remove("language_code");
	    	valuemap.put("language_code", valuelist.get(0).get("language_name"));
	    	valuelist = jdbcTemplate.queryForList("select * from status where status_code=?", valuemap.get("status_code"));
	    	valuemap.remove("status_code");
	    	valuemap.put("status_code", valuelist.get(0).get("status_name"));
	    	
	    	list.set(i, valuemap);
	    }
	    if(list.size() == 0) {
	    	message = "レコードが登録されていません。";
	    }
	    model.addAttribute("message", message);
	    model.addAttribute("projects", list);
		return "list";
	}
	
	@RequestMapping(value="/delete/{arg}", method=RequestMethod.GET)
	public String delete(@PathVariable("arg") Long project_id) {
		jdbcTemplate.update("delete from project where project_id = ?", project_id);
		return "redirect:/list";
	}
	
	@RequestMapping(value="/change/{arg}", method=RequestMethod.GET)
	public String change(Model model, @PathVariable("arg") Long project_id) {
		List<Map<String,Object>> list;
	    list = jdbcTemplate.queryForList("select * from project where project_id=?", project_id);
	    model.addAttribute("project", list.get(0));
		model.addAttribute("projecttypeItems",getSelectedItems("projecttype"));
		model.addAttribute("statusItems",getSelectedItems("status"));
		model.addAttribute("languageItems",getSelectedItems("language"));
		return "change";
	}
	
	@RequestMapping(value="/change/{arg}", method=RequestMethod.POST)
	public String changecheck(Model model, @PathVariable("arg") Long project_id,
										   @RequestParam(value="language_code") String language_code,
										   @RequestParam(value="summary") String summary,
										   @RequestParam(value="status_code") String status_code,
										   @RequestParam(value="customer") String customer,
										   @RequestParam(value="charge") String charge,
										   @RequestParam(value="reviewer") String reviewer,
										   @RequestParam(value="release_date") String release_date,
										   @RequestParam(value="remarks") String remarks) throws ParseException {
		String message = "";
		List<Map<String,Object>> list;
	    list = jdbcTemplate.queryForList("select * from project where project_id=?", project_id);
	    Map<String,Object> project = list.get(0);
	    if(language_code.equals("")) {
			message = "開発言語を選択してください。<br />";
		}
		
		if(summary.equals("")) {
			message += "概要を入力してください。<br />";
		}
		
		if(status_code.equals("")) {
			message += "状態を選択してください。<br />";
		}
		
		if(!release_date.equals("")) {
			Pattern p = Pattern.compile("^[0-9]{4}/[0-9]{2}/[0-9]{2}$");
	        Matcher m = p.matcher(release_date);
			if(!m.find()) {
				message += "リリース日は yyyy/MM/dd の形式で入力してください。<br />";
			}
			else {
				DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
				try {
				    format.setLenient(false);
				    format.parse(release_date);
				} catch (ParseException e) {
					message += "リリース日は カレンダーに存在しない日付です。<br />";
				}
			}
		}
		if(message.equals("")) {
			if(release_date.equals("")) {
				jdbcTemplate.update("update project set language_code=?,"
						  + "summary=?,status_code=?,customer=?,"
						  + "charge=?,reviewer=?,"
						  + "remarks=? where project_id = ?", 
						  language_code, summary, status_code,
						  customer, charge, reviewer,
						  remarks, project_id);
			}
			else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				Date release_date_date = sdf.parse(release_date);
				jdbcTemplate.update("update project set language_code=?,"
								  + "summary=?,status_code=?,customer=?,"
								  + "charge=?,reviewer=?,release_date=?,"
								  + "remarks=? where project_id = ?", 
								  language_code, summary, status_code,
								  customer, charge, reviewer, release_date_date,
								  remarks, project_id);
			}
			return "redirect:/list";
		}
		project.remove("language_code");
		project.put("language_code", language_code);
		project.remove("summary");
		project.put("summary", summary);
		project.remove("status_code");
		project.put("status_code", status_code);
		project.remove("customer");
		project.put("customer", customer);
		project.remove("charge");
		project.put("charge", charge);
		project.remove("reviewer");
		project.put("reviewer", reviewer);
		project.remove("release_date");
		project.put("release_date", release_date);
		project.remove("remarks");
		project.put("remarks", remarks);
		model.addAttribute("project", project);
		model.addAttribute("projecttypeItems",getSelectedItems("projecttype"));
		model.addAttribute("statusItems",getSelectedItems("status"));
		model.addAttribute("languageItems",getSelectedItems("language"));
		model.addAttribute("message", message);
		return "change";
	}
	
}