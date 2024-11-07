<?php
/**
 * 使用PHP代码从纯真IP数据库的二进制文件中获取IP地址所在地理位置信息
 * @author york
 */
class IpLocationSeekerBinary {
	private $qqwry_filepath;
	private $handle;
	private $first_index_pos;
	private $last_index_pos;
	private $index_count;
	
	/**
	 * 构造函数，初始化一个查询实例
	 * @param string $sqlite_db_path 存放了纯真地址库的sqlite数据库的路径
	 * @param integer $version sqlite数据库的版本，2或者3，默认值为-1，表示自动检测数据库的版本
	 */
	public function __construct($qqwry_filepath){
		$this->qqwry_filepath = $qqwry_filepath;
		
		$this->handle = fopen($this->qqwry_filepath, "r");
		$this->first_index_pos = self::fgetint($this->handle, false);
		$this->last_index_pos = self::fgetint($this->handle, false);
		$this->index_count = ($this->last_index_pos - $this->first_index_pos)/7 + 1;
	}
	
	/**
	 * 查找一个IP地址所对应的地理位置信息
	 * @param mixed $ip_address 由字符串或者整数表示的一个IP地址
	 * @return array IP地址所对应的country和area信息
	 */
	public function seek($ip_address){
		if(is_int($ip_address)) $ip_int = $ip_address;
		else $ip_int = self::ip2int($ip_address);
		
		$ip_record_index = $this->half_find($ip_int, 0, $this->index_count-1);
		fseek($this->handle, $this->first_index_pos + $ip_record_index*7 + 4, SEEK_SET);
		$ip_record_pos = self::fgetint_with_three_bytes($this->handle);
		return $this->get_country_and_area($ip_record_pos);
	}
	

	/**
	 * 把qqwry的二进制文件转存到sqlite数据库当中
	 * @param string $filepath sqlite数据库的文件路径
	 * @param integer $version 要转存的sqlite的版本信息，2代表sqlite2，3代表sqlite3
	 * @param boolean $use_china_province_name 是否对中国地区的第一个字段信息使用省份的名字
	 */
	public function saveAsSqlite($filepath, $version=3, $use_china_province_name=true){
		include_once 'IpLocationSeekerSqlite.php';
		if(file_exists($filepath)) unlink($filepath); // 删掉旧库，重新建库
		$sqlite_seeker = new IpLocationSeekerSqlite($filepath, $version);
		$sql_create_table = "create table qqwry (ip integer primary key,
				country varchar(255)
		);
        CREATE TABLE android_metadata (locale TEXT DEFAULT 'en_US');
        INSERT INTO android_metadata VALUES('en_US')
";
		$sqlite_seeker->execute($sql_create_table);
		$sqlite_seeker->beginTransaction(); // 使用事务来加快数据的插入速度
		
		for($i=0; $i<$this->index_count; $i++){
			$ip_index_pos = $this->first_index_pos + $i * 7;
			$ip_record_pos_pos = $ip_index_pos + 4;
			
			fseek($this->handle, $ip_index_pos, SEEK_SET);
			$ip_int = self::fgetint($this->handle, false); // 这一条记录所对应的ip地址
			
			fseek($this->handle, $ip_record_pos_pos, SEEK_SET);
			$ip_record_pos = self::fgetint_with_three_bytes($this->handle);
			list($country, $area) = $this->get_country_and_area($ip_record_pos);

			// echo $country, ", ", $area, "\n";

			$country = str_replace("中国–", "", $country);
			$country = str_replace("加利福尼亚州", "加州", $country);
			$country = str_replace("德克萨斯州", "德州", $country);
			$country = str_replace("加利福尼亚州", "加州", $country);
			$country = str_replace("北京–北京", "北京", $country);
			$country = str_replace("天津-天津", "天津", $country);
			$country = str_replace("上海-上海", "上海", $country);
			$country = str_replace("重庆-重庆", "重庆", $country);
			// 中建连字符貌似是中文的？
			$country = str_replace("北京–北京", "北京", $country);
			$country = str_replace("天津–天津", "天津", $country);
			$country = str_replace("上海–上海", "上海", $country);
			$country = str_replace("重庆–重庆", "重庆", $country);



			$area = str_replace("'", "", $area);
			$area = str_replace("\t", "", $area);
			$area = str_replace("\n", "", $area);
			$area = str_replace("(", "-", $area);
			$area = str_replace(")", "-", $area);
			$area = str_replace("/", "", $area);
			$area = str_replace("CZ88.NET", "", $area);
			$area = str_replace("互联网有限公司", "公司", $area);
			$area = str_replace("有限公司", "公司", $area);
			$area = str_replace("Level3", "", $area);
			$area = rtrim($area, '-');

			// echo $country, "--", $area, "\n";
			
			// $country = str_replace("\t", "", $country);
			// $country = str_replace("\n", "", $country);
			// $country = str_replace("地区", "", $country);
			// $country = str_replace("省", "", $country);
			// $country = str_replace("市", "", $country);
			// $country = str_replace("县", "", $country);
			// $country = str_replace("区", "", $country);
			// $country = str_replace("清华大学", "清华", $country);
			// $country = str_replace("CZ88.NET", "", $country);
			// if($use_china_province_name) $country = self::real_province_name($country);

			// echo long2ip($ip_int), "\t{", $country, "}\t{", $area, "}\n";

			$detail = "";
			$result = $country;
			if(strlen($area) > 1) { // 一个字符的，忽略掉，估计也不是啥有用信息
				$result = $country."-".$area;				
			}
			// if(strpos($area, '大学') !== false && strlen($detail) == 0) {
			// 	$detail = substr($area, 0, strpos($area, '大学') + strlen('大学'));
			// 	// echo $detail, "\n";
			// 	$result = $country."-".$detail;
			// 	// echo $country, "\n";
			// }

			// if(strpos($area, '学院') !== false && strlen($detail) == 0) {
			// 	$detail = substr($area, 0, strpos($area, '学院') + strlen('学院'));
			// 	// echo $detail, "\n";
			// 	$result = $country."-".$detail;
			// 	// echo $country, "\n";
			// }

			// if(strpos($area, '公司') !== false && strlen($detail) == 0) {
			// 	$detail = substr($area, 0, strpos($area, '公司') + strlen('公司'));
			// 	// echo $detail, "\n";
			// 	$result = $country."-".$detail;
			// 	// echo $country, "\n";
			// }

			// if(strpos($area, '中心') !== false && strlen($detail) == 0) {
			// 	$detail = substr($area, 0, strpos($area, '中心') + strlen('中心'));
			// 	// echo $detail, "\n";
			// 	$result = $country."-".$detail;
			// 	// echo $country, "\n";
			// }

			// if(strpos($area, '州') !== false && strlen($detail) == 0) {
			// 	$detail = substr($area, 0, strpos($area, '州') + strlen('州'));
			// 	// echo $detail, "\n";
			// 	$result = $country."-".$detail;
			// 	// echo $country, "\n";
			// }

			$result = rtrim($result, '-');

			// echo "{", $country, ",", $area, "} -> {", $result, "}\n";
			$sql = "insert into qqwry (ip, country) values ({$ip_int}, '{$result}')";
			$sqlite_seeker->execute($sql);
		}
		
		$sqlite_seeker->commit();
	}
	
	/**
	 * 从纯真数据库文件中提取到的国家信息中提取中国国内的省份信息
	 * @param string $country_in_qqwry
	 * @return string
	 */
	private static function real_province_name($country_in_qqwry){
		$country_in_qqwry = trim($country_in_qqwry);
		$country_in_qqwry = str_replace("，", "", $country_in_qqwry);
		static $special_maps = array("东北农业大学"=>"黑龙江", "东北大学"=>"辽宁", 
				"东北林业大学"=>"黑龙江", "东华大学"=>"上海", "东华理工大学"=>"江西", "中央财经大学"=>"北京",
				"东南大学"=>"江苏", "中北大学"=>"山西", "中南大学"=>"湖南", "中南财经政法大学"=>"湖北",
				"中山大学"=>"广东", "中科院"=>"北京", "中经网"=>"北京", "佳木斯大学"=>"黑龙江", 
				"华东交通大学"=>"江西", "华东师范大学"=>"上海", "华东理工大学"=>"上海", "华中农业大学"=>"湖北",
				"华中农业大学学生宿舍"=>"湖北", "华中科技大学东"=>"湖北", "华北科技学院"=>"河北",
				"南京化工大学"=>"江苏", "南京大学"=>"江苏", "南京工业大学"=>"江苏", "南京理工大学"=>"江苏",
				"南开大学"=>"天津", "南昌大学"=>"江西", "南昌工程学院"=>"江西", "南昌理工学院"=>"江西",
				"哈尔滨工业大学"=>"黑龙江", "哈尔滨工程大学"=>"黑龙江", "哈尔滨师范大学"=>"黑龙江", "哈尔滨理工大学"=>"黑龙江",
				"大庆职工大学"=>"黑龙江", "太原科技大学"=>"山西", "汉中理工学院"=>"陕西", "西北工业大学"=>"陕西",
				"郑州大学"=>"河南", "长江大学"=>"湖北", "对外经济贸易大学"=>"北京", "宁波大学"=>"浙江",
				"黄河科技大学"=>"河南", "长江大学东校区"=>"湖北", "华北地区"=>"中国", "华东地区"=>"中国");
		if(isset($special_maps[$country_in_qqwry])) return $special_maps[$country_in_qqwry];
		static $province_names = NULL;
		if(!$province_names)
			$province_names = explode(",", "北京,上海,天津,重庆,安徽,福建,甘肃,广东,广西,贵州,海南,"
				."河北,河南,黑龙江,湖北,湖南,吉林,江苏,江西,辽宁,内蒙古,宁夏,青海,山东,山西,陕西,四川,西藏,"
				."新疆,云南,浙江,香港,澳门,台湾,美国");
		foreach($province_names as $name){
			if(strpos($country_in_qqwry, $name) !== false) return $name;
		}
		if(strpos($country_in_qqwry, "北方工业大学")!==false) return "北京";
		if(strpos($country_in_qqwry, "华南农业大学")!==false) return "广东";
		if(strpos($country_in_qqwry, "华南理工大学")!==false) return "广东";
		if(strpos($country_in_qqwry, "厦门大学")!==false) return "福建";
		if(strpos($country_in_qqwry, "同济大学")!==false) return "上海";
		if(strpos($country_in_qqwry, "大连理工大学")!==false) return "辽宁";
		if(strpos($country_in_qqwry, "成都")!==false) return "四川";
		if(strpos($country_in_qqwry, "武汉")!==false) return "湖北";
		if(strpos($country_in_qqwry, "西安")!==false) return "陕西";
		if(strpos($country_in_qqwry, "西华大学")!==false) return "四川";
		if(strpos($country_in_qqwry, "长春")!==false) return "吉林";
		if(strpos($country_in_qqwry, "长沙")!==false) return "湖南";
		if(strpos($country_in_qqwry, "首都")!==false) return "北京";
		if(strpos($country_in_qqwry, "青岛")!==false) return "山东";
		if(strpos($country_in_qqwry, "齐齐哈尔")!==false) return "黑龙江";
		if(strpos($country_in_qqwry, "暨南大学")!==false) return "广东";
		if(strpos($country_in_qqwry, "清华大学")!==false) return "北京";
		if(strpos($country_in_qqwry, "xrea.com")!==false) return "xrea.com";
		if(strpos($country_in_qqwry, "福州大学")!==false) return "福建";
		if(strpos($country_in_qqwry, "集美大学")!==false) return "福建";
		if(strpos($country_in_qqwry, "华中科技大学")!==false) return "湖北";
		if(strpos($country_in_qqwry, "对外经济贸易大学")!==false) return "北京";
		if(strpos($country_in_qqwry, "中国农业大学")!==false) return "北京";
		if(strpos($country_in_qqwry, "哈尔滨")!==false) return "黑龙江";
		if(strpos($country_in_qqwry, "中南大学")!==false) return "湖南";
		if(strpos($country_in_qqwry, "合肥")!==false) return "安徽";
		if(strpos($country_in_qqwry, "中国人民大学")!==false) return "北京";
		if(strpos($country_in_qqwry, "南京")!==false) return "江苏";
		if(strpos($country_in_qqwry, "中国国际电子商务中心")!==false) return "北京";
		if(strpos($country_in_qqwry, "郑州")!==false) return "河南";
		if(strpos($country_in_qqwry, "中国测绘院")!==false) return "北京";
		if(strpos($country_in_qqwry, "中国农业科学院")!==false) return "北京";
		if(strpos($country_in_qqwry, "IBM中国公司")!==false) return "北京";
		if(strpos($country_in_qqwry, "雅虎中国")!==false) return "北京";
		if(strpos($country_in_qqwry, "兰州")!==false) return "甘肃";
		if(strpos($country_in_qqwry, "华侨大学")!==false) return "福建";
		
		return $country_in_qqwry;
	}

	/**
	 * 使用二分法查找一个ip地址在纯真数据库文件当中所对应的数据位置
	 * @param integer $ip_int
	 * @param integer $index_low
	 * @param integer $index_high
	 */
	private function half_find($ip_int, $index_low, $index_high){
		if ($index_high - $index_low == 1) return $index_low;
		$index_middle = intval( ($index_low + $index_high) / 2 );
		$file_offset = $this->first_index_pos + $index_middle * 7;
		fseek($this->handle, $file_offset, SEEK_SET);
		$ip_middle_int = self::fgetint($this->handle, false);
		if ($ip_middle_int == $ip_int) return $index_low;
		elseif (($ip_int > $ip_middle_int && $ip_middle_int > 0) || ( $ip_int < 0 && ($ip_int > $ip_middle_int || $ip_middle_int > 0 ))){
			$index_low = $index_middle;
			return $this->half_find($ip_int, $index_low, $index_high, $this->first_index_pos);
		}else{
			$index_high = $index_middle;
			return $this->half_find($ip_int, $index_low, $index_high, $this->first_index_pos);
		}
	}
	
	
	/**
	 * 获取某一条IP地址所对应的国家和地区信息，返回一个数组
	 * @param int $ip_record_pos 该ip地址信息所在的文件位置
	 * @return array
	 */
	private function get_country_and_area($ip_record_pos){
		$country = $area = "";
		
		fseek($this->handle, $ip_record_pos+4, SEEK_SET);
		$flag = ord(fgetc($this->handle));
		if($flag == 1){ #the next three bytes are another pointer
			$ip_record_level_two_pos = self::fgetint_with_three_bytes($this->handle);
			fseek($this->handle, $ip_record_level_two_pos, SEEK_SET);
			$level_two_flag = ord(fgetc($this->handle));
			if($level_two_flag == 2){
				$ip_record_level_three_pos = self::fgetint_with_three_bytes($this->handle);
				$level_three_flag = ord(fgetc($this->handle));
				fseek($this->handle, $ip_record_level_three_pos, SEEK_SET);
				$country = self::fgets_zero_end($this->handle);
				
				if($level_three_flag==1 || $level_three_flag==2){
					fseek($this->handle, $ip_record_level_two_pos+5, SEEK_SET);
					$ip_record_area_string_pos = self::fgetint_with_three_bytes($this->handle);
					fseek($this->handle, $ip_record_area_string_pos, SEEK_SET);
					$area = self::fgets_zero_end($this->handle);
				}else{
					fseek($this->handle, $ip_record_level_two_pos+4, SEEK_SET);
					$area = self::fgets_zero_end($this->handle);
				}
			}else{
				fseek($this->handle, $ip_record_level_two_pos, SEEK_SET);
				$country = self::fgets_zero_end($this->handle);
				$area = self::fgets_zero_end($this->handle);
			}
		}elseif($flag == 2){
			$ip_record_level_two_pos = self::fgetint_with_three_bytes($this->handle);
			fseek($this->handle, $ip_record_level_two_pos, SEEK_SET);
			$country = self::fgets_zero_end($this->handle);
			fseek($this->handle, $ip_record_pos+8, SEEK_SET);
			$area = self::fgets_zero_end($this->handle);
		}else{
			fseek($this->handle, $ip_record_pos+4, SEEK_SET);
			$country = self::fgets_zero_end($this->handle);
			$area = self::fgets_zero_end($this->handle);
		}
		
		$country = iconv("gb18030", "utf-8//IGNORE", $country);
		if($area){
			if(ord($area[0]) == 2) $area = ""; // 不规则字符
			else $area = iconv("gb18030", "utf-8//IGNORE", $area);
		}
		return array($country, $area);
	}

	/**
	 * 从文件的当前位置读取一个以\0结尾的字符串
	 * @param resource $handle 文件指针
	 */
	private static function fgets_zero_end($handle){
		$result = "";
		while(true){
			$char = fgetc($handle);
			if(ord($char) == 0) break;
			$result .= $char;
		}
		return $result;
	}
	
	/**
	 * 读取文件的一个字节
	 * @param resource $handle 文件指针
	 * @return int
	 */
	private static function fget($handle){
		$char = fgetc($handle);
		return ord($char);
	}
	
	/**
	 * 读取一个四个字节的整数
	 * @param resource $handle 文件指针
	 * @param boolean $big 是否采用大端法读取，如果要使用小端法，传入false
	 */
	private static function fgetint($handle, $big=true){
		static $int_max = 2147483647;
		if($big)
			$int = self::fget($handle) << 24
					 | self::fget($handle) << 16
					 | self::fget($handle) <<  8
					 | self::fget($handle);
		else
			$int = self::fget($handle)
					 | self::fget($handle) <<  8
					 | self::fget($handle) << 16
					 | self::fget($handle) << 24;
		if($int <= $int_max){
			return $int;
		}else{
			return $int - $int_max - $int_max - 2;
		}
	}
	/**
	 * 采用小端法读取一个只用三个字节表示的整数
	 * @param resource $handle 文件指针
	 */
	private static function fgetint_with_three_bytes($handle){
		return self::fget($handle)
				 | self::fget($handle) <<  8
				 | self::fget($handle) << 16;
	}

	/**
	 * php 自带的 ip2long 函数在 32 位环境下会输出负数，而在 64 位的情况下不会输出负数
	 * 该函数将字符串形式的ip地址转化成一个整数值，并使在 64 环境下运行时按照 32 位条件一样统一输出负数
	 * @param string $ip_address
	 * @return int
	 */
	public static function ip2int($ip_address){
		static $int_max = 2147483647;
		$iplong = ip2long($ip_address);
		if($iplong === false) return 0;
		elseif($iplong <= $int_max){
			return $iplong;
		}else{
			$iplong = $iplong - $int_max - $int_max - 2;
			return $iplong;
		}
	}
}

