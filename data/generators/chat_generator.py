import random
import uuid
from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class ChatGenerator(BaseGenerator):
    # 한국어 채팅 메시지 템플릿
    CHAT_MESSAGES = [
        # 인사/소개
        "안녕하세요! 연락 감사합니다.",
        "반갑습니다! 좋은 기회가 되길 바랍니다.",
        "안녕하세요, 제안 주셔서 감사합니다!",
        "연락 기다리고 있었어요!",
        # 협업 관련
        "캠페인 관련해서 문의드립니다.",
        "제안해 주신 내용 검토해 봤어요.",
        "협업 조건에 대해 상담 가능할까요?",
        "일정 조율이 가능할까요?",
        "촬영 일정 확인 부탁드립니다.",
        "컨펌 부탁드릴게요!",
        # 제품/콘텐츠 관련
        "제품 받았습니다! 잘 확인했어요.",
        "콘텐츠 초안 보내드렸습니다.",
        "피드백 주시면 수정 반영하겠습니다.",
        "수정본 확인해 주세요!",
        "업로드 완료했습니다!",
        # 긍정적 응답
        "네, 좋습니다!",
        "알겠습니다!",
        "확인했습니다, 감사합니다.",
        "좋은 제안이네요!",
        "진행하면 될 것 같아요!",
        # 질문
        "혹시 리워드는 언제쯤 지급되나요?",
        "제품 발송은 언제쯤 될까요?",
        "콘텐츠 가이드라인이 있을까요?",
        "업로드 기한이 어떻게 되나요?",
        "추가 협찬 가능한가요?",
        # 일반 대화
        "네, 확인해 보고 다시 연락드릴게요.",
        "잠시만요, 확인 중이에요.",
        "말씀하신 대로 진행하겠습니다.",
        "다음에 또 좋은 기회로 뵙겠습니다!",
        "감사합니다, 좋은 하루 되세요!"
    ]

    def _get_table_columns(self, table_name):
        """테이블 컬럼 목록 조회"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"DESCRIBE {table_name}")
                return [row['Field'] for row in cursor.fetchall()]
        except Exception:
            return []

    def _table_exists(self, table_name):
        """테이블 존재 여부 확인"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
                return cursor.fetchone() is not None
        except Exception:
            return False

    def _get_enum_values(self, table_name, column_name):
        """ENUM 컬럼의 허용 값 조회"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW COLUMNS FROM {table_name} WHERE Field = %s", (column_name,))
                result = cursor.fetchone()
                if result and result.get('Type', '').startswith('enum'):
                    # enum('VALUE1','VALUE2') 형태에서 값 추출
                    enum_str = result['Type']
                    values = enum_str.replace("enum(", "").replace(")", "").replace("'", "").split(",")
                    return [v.strip() for v in values]
        except Exception:
            pass
        return []

    def generate_chat_rooms(self, room_count=20):
        """채팅방 생성"""
        print(f"\n[채팅방] {room_count}개의 채팅방 생성 중...")

        if not self._table_exists('chat_room'):
            print("  - chat_room 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('chat_room')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'BRAND'")
            brand_user_ids = [row['id'] for row in cursor.fetchall()]

        if not creator_ids or not brand_user_ids:
            print("[경고] 크리에이터 또는 브랜드 사용자가 없습니다.")
            return

        # ENUM 값 동적 조회
        room_types = self._get_enum_values('chat_room', 'room_type') or self._get_enum_values('chat_room', 'type')
        if not room_types:
            room_types = ['DIRECT']  # 기본값

        rooms = []
        for i in range(room_count):
            data = {}
            if 'room_key' in columns:
                data['room_key'] = str(uuid.uuid4())
            if 'name' in columns:
                data['name'] = f"채팅방 {i + 1}"
            if 'title' in columns:
                data['title'] = f"채팅방 {i + 1}"
            if 'type' in columns:
                type_values = self._get_enum_values('chat_room', 'type') or room_types
                data['type'] = self.fake.random_element(type_values)
            if 'room_type' in columns:
                data['room_type'] = self.fake.random_element(room_types)
            if 'created_by' in columns:
                data['created_by'] = self.fake.random_element(creator_ids + brand_user_ids)
            if 'is_deleted' in columns:
                data['is_deleted'] = False
            if 'created_at' in columns:
                data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 60))
            if 'updated_at' in columns:
                data['updated_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
            if data:
                rooms.append(data)

        if rooms:
            cols = list(rooms[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT INTO chat_room ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, rooms, "채팅방")

    def generate_chat_room_members(self):
        """채팅방 멤버 생성"""
        print(f"\n[채팅방 멤버] 채팅방 멤버 생성 중...")

        if not self._table_exists('chat_room_member'):
            print("  - chat_room_member 테이블이 없습니다. 스킵합니다.")
            return

        if not self._table_exists('chat_room'):
            print("  - chat_room 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('chat_room_member')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM chat_room")
            room_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users")
            user_ids = [row['id'] for row in cursor.fetchall()]

        if not room_ids or not user_ids:
            print("[경고] 채팅방 또는 사용자가 없습니다.")
            return

        members = []
        for room_id in room_ids:
            # 각 채팅방에 2~5명의 멤버 추가
            num_members = self.fake.random_int(2, min(5, len(user_ids)))
            selected_users = self.fake.random_sample(user_ids, length=num_members)

            for user_id in selected_users:
                data = {}
                if 'chat_room_id' in columns:
                    data['chat_room_id'] = room_id
                if 'room_id' in columns:
                    data['room_id'] = room_id
                if 'user_id' in columns:
                    data['user_id'] = user_id
                if 'joined_at' in columns:
                    data['joined_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                if 'is_active' in columns:
                    data['is_active'] = True
                if 'created_at' in columns:
                    data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    members.append(data)

        if members:
            cols = list(members[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO chat_room_member ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, members, "채팅방 멤버")

    def generate_chat_messages(self, messages_per_room=10):
        """채팅 메시지 생성"""
        print(f"\n[채팅 메시지] 채팅 메시지 생성 중...")

        if not self._table_exists('chat_message'):
            print("  - chat_message 테이블이 없습니다. 스킵합니다.")
            return

        if not self._table_exists('chat_room_member'):
            print("  - chat_room_member 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('chat_message')

        with self.connection.cursor() as cursor:
            # 채팅방별 멤버 조회
            room_id_col = 'chat_room_id' if 'chat_room_id' in self._get_table_columns('chat_room_member') else 'room_id'
            cursor.execute(f"SELECT {room_id_col} as room_id, user_id FROM chat_room_member")
            room_members = cursor.fetchall()

        if not room_members:
            print("[경고] 채팅방 멤버가 없습니다.")
            return

        # 채팅방별 멤버 그룹화
        rooms = {}
        for rm in room_members:
            room_id = rm['room_id']
            if room_id not in rooms:
                rooms[room_id] = []
            rooms[room_id].append(rm['user_id'])

        # ENUM 값 동적 조회
        message_types = self._get_enum_values('chat_message', 'message_type') or self._get_enum_values('chat_message', 'type')
        if not message_types:
            message_types = ['TEXT']  # 기본값

        messages = []

        for room_id, user_ids in rooms.items():
            for _ in range(messages_per_room):
                data = {}
                if 'chat_room_id' in columns:
                    data['chat_room_id'] = room_id
                if 'room_id' in columns:
                    data['room_id'] = room_id
                if 'sender_id' in columns:
                    data['sender_id'] = self.fake.random_element(user_ids)
                if 'user_id' in columns:
                    data['user_id'] = self.fake.random_element(user_ids)
                if 'content' in columns:
                    data['content'] = random.choice(self.CHAT_MESSAGES)
                if 'message' in columns:
                    data['message'] = random.choice(self.CHAT_MESSAGES)
                if 'message_type' in columns:
                    msg_types = self._get_enum_values('chat_message', 'message_type') or message_types
                    data['message_type'] = self.fake.random_element(msg_types)
                if 'type' in columns:
                    type_values = self._get_enum_values('chat_message', 'type') or message_types
                    data['type'] = self.fake.random_element(type_values)
                if 'is_read' in columns:
                    data['is_read'] = self.fake.boolean(chance_of_getting_true=70)
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now() - timedelta(hours=self.fake.random_int(0, 720))
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    messages.append(data)

        if messages:
            cols = list(messages[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT INTO chat_message ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, messages, "채팅 메시지")

    def generate_all(self, room_count=20, messages_per_room=10):
        self.generate_chat_rooms(room_count)
        self.generate_chat_room_members()
        self.generate_chat_messages(messages_per_room)
