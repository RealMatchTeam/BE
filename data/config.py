import os
import sys
import pymysql
from dotenv import load_dotenv

load_dotenv()


class DatabaseConfig:
    def __init__(self):
        self.host = os.getenv('MYSQL_HOST', 'localhost')
        self.port = int(os.getenv('MYSQL_PORT', 3306))
        self.user = os.getenv('MYSQL_USER')
        self.password = os.getenv('MYSQL_PASSWORD')
        self.database = os.getenv('MYSQL_DATABASE')

    def get_connection(self):
        try:
            connection = pymysql.connect(
                host=self.host,
                port=self.port,
                user=self.user,
                password=self.password,
                database=self.database,
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            print(f"[성공] 데이터베이스 연결 성공: {self.database}")
            return connection
        except Exception as e:
            print(f"[오류] 데이터베이스 연결 실패: {e}")
            sys.exit(1)
