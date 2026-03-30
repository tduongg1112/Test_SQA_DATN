import mysql.connector
from mysql.connector import Error

import os

class Database:
    def __init__(self):
        self.config = {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', 3306)),
            'database': 'schema_chatbot',
            'user': 'root',
            'password': os.getenv('DB_PASSWORD', 'Duong111203')
        }
    
    def get_connection(self, include_db=True):
        """Tạo connection đến MySQL"""
        try:
            cfg = self.config.copy()
            if not include_db:
                del cfg['database']
            connection = mysql.connector.connect(**cfg)
            return connection
        except Error as e:
            print(f"Error connecting to MySQL: {e}")
            return None
    
    def init_database(self):
        """Khởi tạo bảng trong database"""
        conn_init = self.get_connection(include_db=False)
        if conn_init:
            cursor_init = conn_init.cursor()
            cursor_init.execute(f"CREATE DATABASE IF NOT EXISTS {self.config['database']} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
            cursor_init.close()
            conn_init.close()

        connection = self.get_connection()
        if connection is None:
            return False
        
        try:
            cursor = connection.cursor()
            
            # Tạo bảng chat_metrics
            create_table_query = """
            CREATE TABLE IF NOT EXISTS chat_metrics (
                id INT AUTO_INCREMENT PRIMARY KEY,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                input_text MEDIUMTEXT,
                output_text MEDIUMTEXT,
                input_tokens INT,
                output_tokens INT,
                total_tokens INT,
                ttft_ms FLOAT COMMENT 'Time to First Token in milliseconds',
                total_time_ms FLOAT COMMENT 'Total response time in milliseconds',
                status VARCHAR(50),
                error_message TEXT,
                INDEX idx_created_at (created_at),
                INDEX idx_status (status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """
            
            cursor.execute(create_table_query)
            connection.commit()
            print("✓ Database table 'chat_metrics' created successfully")
            return True
            
        except Error as e:
            print(f"Error creating table: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
    
    def insert_metrics(self, input_text, output_text, input_tokens, output_tokens, 
                      ttft_ms, total_time_ms, status='success', error_message=None):
        """Lưu metrics vào database"""
        connection = self.get_connection()
        if connection is None:
            return False
        
        try:
            cursor = connection.cursor()
            
            insert_query = """
            INSERT INTO chat_metrics 
            (input_text, output_text, input_tokens, output_tokens, total_tokens, 
             ttft_ms, total_time_ms, status, error_message)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            
            total_tokens = input_tokens + output_tokens
            
            values = (
                input_text,
                output_text,
                input_tokens,
                output_tokens,
                total_tokens,
                ttft_ms,
                total_time_ms,
                status,
                error_message
            )
            
            cursor.execute(insert_query, values)
            connection.commit()
            return True
            
        except Error as e:
            print(f"Error inserting metrics: {e}")
            return False
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()