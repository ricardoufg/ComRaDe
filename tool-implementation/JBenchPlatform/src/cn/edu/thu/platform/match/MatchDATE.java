package cn.edu.thu.platform.match;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.thu.platform.entity.Race;
import cn.edu.thu.platform.entity.Report;
import cn.edu.thu.platform.entity.Reports;
import cn.edu.thu.platform.frame.MainFrame;

public class MatchDATE  implements MatchInterface{

	public MatchDATE() {
		System.out.println("Start DATE matching...");
	}
	
	@Override
	public void matchFile() {
		// TODO Auto-generated method stub
		File file = new File(MainFrame.path+"result.txt");
		BufferedReader reader = null;
		String info = "";
		// state: represent the current matching state,
		// 0--initial state; 1--the state which is '>>>start<<<';
		int state = 0;
		
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = "";
			String costTime="";
			while((tempString = reader.readLine())!= null){

				switch(state) {
					case 0://when the state is 0, it starts to read the result.txt file, wants to find the position containing'>>>start'.
						if(tempString.indexOf(">>>start")!=-1) {//find the start position
							state=1;
						}
						//stitch the contents of the result.txt into a single line
						info +=tempString;
						break;
					case 1://when the state is 1, it has found the start position of result.txt, wants to find the position containing'>>>>>end<<<<<'.
						if(tempString.indexOf(">>>>>end<<<<<")!=-1) {//find the end position
							state=2;
						}					
						if(tempString.indexOf("[total time:")!=-1) {//find the position recording time spent 
							costTime=tempString.substring(tempString.indexOf(":")+1, tempString.length()-1);
						}
						//stitch the contents of the result.txt into a single line
						info +=tempString;
						if(state==1){
							break;
						}
						//start to extract important data race information, such as line number.
//						System.out.println("\n\n\n"+info+"\n");
						String caseName = "";
						//this pattern is used for finding test case name
						String pattern = "((>>>>>start.*?about\\s+)(.*?)(on\\s+.*?<<<<<))(.*)";
						Pattern namePattern = Pattern.compile(pattern);
						Matcher nameMatch = namePattern.matcher(info);
						if (nameMatch.find()) {
							//it represents the name of test case
							caseName = nameMatch.group(3).trim();
							System.out.println("caseName"+caseName);
							Set<Race> races = new HashSet<Race>();
							//this pattern is used for extracting line number.
							//String pair = "(.*?Race Pair:\\s?.*?\\.java:\\s*)(\\d+?)(\\s+?.*?\\.java:\\s*)(\\d+)(\\s*?)(.*)";
							String pair = "(.*?Race Pair:\\s+?)(.+?)(\\.java)(:\\s*?)(\\d+?)(\\s+?---\\s+?)(.+?)(\\.java)(:\\s*?)(\\d+)(\\s*?)(.*)";
							Pattern pairPattern = Pattern.compile(pair);
							info = nameMatch.group(5);
							Matcher pairMatch = pairPattern.matcher(info);
							while(pairMatch.find()) {
								//the first class name
//								String str1 = pairMatch.group(2);
								//the first line number
								String class1 = pairMatch.group(2);
								String num1 = pairMatch.group(5);
								//the second class name
//								String str2 = pairMatch.group(7);
								//the second line number
								String class2 = pairMatch.group(7);
								String num2 = pairMatch.group(10);
								
								//Place the smaller line number in the first position 
								int x1 = Integer.parseInt(num1.trim());
								int x2 = Integer.parseInt(num2.trim());
								System.out.println("line1 and line2:"+x1+"  "+x2);
								System.out.println(class1+ "---" + class2);
								String packageClass = "";
								if(x1<x2) {
									packageClass = class1 + ".java;" + class2 + ".java";
									Race race = new Race(num1.trim(),num2.trim(),packageClass);
									races.add(race);
								}else {
									packageClass = class2 + ".java;" + class1 + ".java";
									Race race = new Race(num2.trim(),num1.trim(),packageClass);
									races.add(race);			
								}
								//start to look for the next match of data race in the specified test case
								pairMatch = pairPattern.matcher(pairMatch.group(12));
							}
							if(!Reports.userNames.contains(caseName)){
								Reports.userNames.add(caseName);
							}
							Report report = new Report(caseName,costTime,races);
							Reports.userReports.put(caseName, report);
						}
						//start to handle the next test case
						tempString = "";
						state = 0;
						break;
						
					default:break;
				}
			}
		}catch (IOException e) {
				e.printStackTrace();
		}finally{
			if (reader != null) {
				try {
						reader.close();
					} catch (IOException e1) {
				}
			}
		}
	}

}
