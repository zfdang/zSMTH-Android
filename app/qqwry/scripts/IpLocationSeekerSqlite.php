<?php
/**
 * 使用PHP代码从纯真IP数据库转换了之后的sqlite数据库中获取IP地址所在地理位置信息
 * @author york
 */
class IpLocationSeekerSqlite {
	private $sqlite_db_path;
	private $sqlite_version;
	private $db;
	
	/**
	 * 构造函数，初始化一个查询实例
	 * @param string $sqlite_db_path 存放了纯真地址库的sqlite数据库的路径
	 * @param integer $version sqlite数据库的版本，2或者3，默认值为-1，表示自动检测数据库的版本
	 */
	public function __construct($sqlite_db_path, $version=-1){
		$this->sqlite_db_path = $sqlite_db_path;
		$this->sqlite_version = $version;
		$this->connectSqliteDb();
	}
	
	/**
	 * 连接sqlite数据库
	 */
	private function connectSqliteDb(){
		if($this->sqlite_version != 2 && $this->sqlite_version != 3){
			// auto detect which sqlite version to use
			if(class_exists("PDO")){ // if PDO exists, try the db as sqlite3
				$this->db = new PDO("sqlite:{$this->sqlite_db_path}");
				$errorCode = $this->db->query("select * from qqwry limit 1")->errorCode();
				if($errorCode != "00000"){
					// error occurred when trying to connect the db file using sqlite3
					$this->sqlite_version = 2;
					$this->db = sqlite_open($this->sqlite_db_path);
				}else{
					$this->sqlite_version = 3;
				}
			}
		}elseif($this->sqlite_version == 3){
			$this->db = new PDO("sqlite:{$this->sqlite_db_path}");
		}else{
			$this->db = sqlite_open($sqlite_filepath);
		}
	}
	
	/**
	 * 执行一条数据库语句
	 * @param string $sql
	 * @throws Exception
	 */
	public function execute($sql){
		if(!$this->db) throw new Exception("sqlite db not connected error");
		if($this->sqlite_version == 2){
			sqlite_exec($this->db, $sql);
		}else{
			$this->db->exec($sql);
		}
	}
	
	/**
	 * 执行一个数据库查询语句，并返回第一行结果
	 * @param string $sql
	 * @return array
	 * @throws Exception
	 */
	public function queryAndFetch($sql){
		if(!$this->db) throw new Exception("sqlite db not connected error");
		if($this->sqlite_version == 2){
			return sqlite_fetch_array(sqlite_unbuffered_query($this->db, $sql));
		}else{
			return $this->db->query($sql)->fetch();
		}
	}
	
	/**
	 * 开始一个事务
	 * @throws Exception
	 */
	public function beginTransaction(){
		if(!$this->db) throw new Exception("sqlite db not connected error");
		if($this->sqlite_version == 2){
			sqlite_exec($this->db, "begin");
		}else{
			$this->db->beginTransaction();
		}
	}
	
	/**
	 * 提交事务
	 * @throws Exception
	 */
	public function commit(){
		if(!$this->db) throw new Exception("sqlite db not connected error");
		if($this->sqlite_version == 2){
			sqlite_exec($this->db, "commit");
		}else{
			$this->db->commit();
		}
	}
	
	/**
	 * 查找一个IP地址所对应的地理位置信息
	 * @param mixed $ip_address 由字符串或者整数表示的一个IP地址
	 * @return array IP地址所对应的country和area信息
	 */
	public function seek($ip_address){
		if(is_int($ip_address)) $ip_int = $ip_address;
		else $ip_int = self::ip2int($ip_address);
		
		$sql = "select * from qqwry where ip<{$ip_int} order by ip desc limit 1";
		$row = $this->queryAndFetch($sql);
		return array($row['country'], $row['area']);
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

