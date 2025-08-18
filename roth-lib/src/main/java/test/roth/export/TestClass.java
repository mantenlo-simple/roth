package test.roth.export;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.roth.export.annotation.JsonCollection;
import com.roth.export.annotation.JsonMap;

public class TestClass {
	private String name;
	private Integer num;
	private Long id;
	private Double value;
	private LocalDateTime ldt;
	private LocalDate ld;
	private java.util.Date jud;
	private java.sql.Date jsd;
	private Boolean isIt;
	private TestEnum ott;
	private Map<String,String> map;
	private List<Integer> list;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Integer getNum() { return num; }
	public void setNum(Integer num) { this.num = num; }
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Double getValue() { return value; }
	public void setValue(Double value) { this.value = value; }
	
	public LocalDateTime getLdt() { return ldt; }
	public void setLdt(LocalDateTime ldt) { this.ldt = ldt; }
	
	public LocalDate getLd() { return ld; }
	public void setLd(LocalDate ld) { this.ld = ld; }
	
	public java.util.Date getJud() { return jud; }
	public void setJud(java.util.Date jud) { this.jud = jud; }
	
	public java.sql.Date getJsd() { return jsd; }
	public void setJsd(java.sql.Date jsd) { this.jsd = jsd; }
	
	public Boolean getIsIt() { return isIt; }
	public void setIsIt(Boolean isIt) { this.isIt = isIt; }
	
	public TestEnum getOtt() { return ott; }
	public void setOtt(TestEnum ott) { this.ott = ott; }
	
	public Map<String, String> getMap() { return map; }
	@JsonMap(keyClass = String.class, valueClass = String.class)
	public void setMap(Map<String, String> map) { this.map = map; }
	
	public List<Integer> getList() { return list; }
	@JsonCollection(elementClass = Integer.class)
	public void setList(List<Integer> list) { this.list = list; }
}
