QQIP地址数据库（纯真版）

## the steps to update qqwry.dat:

#### 1.download latest qqwry.dat from the following website:

	http://www.cz88.net/fox/ipdat.shtml
	
put qqwrt.data in this folder

#### 2.convert qqwry.dat to sqlite.db with scripts/sqlite_import.php
	
project source: https://github.com/starfalling/php-IpLocationSeeker

	php scripts/sqlite_import.php qqwry.dat

qqwry.db will be generated.

btw: use tool http://www.codeproject.com/Articles/220018/SQLite-Compare-Utility to analyze the change if necessary

#### 3.replace qqwry.db in /assets/databases/qqwry.db

#### 4.increase version in /main/java/com/zfdang/zsmth_android/helpers/GEODatabase.java

	private static final int DATABASE_VERSION = 2;

.END.
