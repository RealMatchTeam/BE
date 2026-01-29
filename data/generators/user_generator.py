from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class UserGenerator(BaseGenerator):
    def generate_users(self, count=50):
        print(f"\n[사용자] {count}명의 사용자 생성 중...")

        roles = ['CREATOR', 'BRAND']
        genders = ['MALE', 'FEMALE', 'NONE']

        users = []
        for i in range(count):
            role = self.fake.random_element(roles)
            gender = self.fake.random_element(genders)
            birth_date = self.fake.date_of_birth(minimum_age=18, maximum_age=45)

            user = {
                'name': self.fake.name(),
                'gender': gender,
                'birth': birth_date,
                'nickname': f"{self.fake.word()}_{self.fake.random_number(digits=4)}",
                'email': self.fake.unique.email(),
                'address': self.fake.address().replace('\n', ' '),
                'detail_address': f"{self.fake.random_int(100, 999)}호",
                'role': role,
                'profile_image_url': f"https://api.dicebear.com/7.x/avataaars/svg?seed={self.fake.uuid4()}",
                'last_login': datetime.now() - timedelta(days=self.fake.random_int(0, 30)),
                'created_at': datetime.now() - timedelta(days=self.fake.random_int(1, 365)),
                'updated_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
            }
            users.append(user)

        sql = """
            INSERT INTO users (name, gender, birth, nickname, email, address,
                               detail_address, role, profile_image_url, last_login,
                               is_deleted, created_at, updated_at)
            VALUES (%(name)s, %(gender)s, %(birth)s, %(nickname)s, %(email)s,
                    %(address)s, %(detail_address)s, %(role)s, %(profile_image_url)s,
                    %(last_login)s, false, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, users, "사용자")

    def generate_notification_settings(self):
        print(f"\n[알림 설정] 알림 설정 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users")
            user_ids = [row['id'] for row in cursor.fetchall()]

        if not user_ids:
            print("[경고] 사용자가 없습니다.")
            return

        notification_types = ['CAMPAIGN', 'MATCH', 'CHAT', 'SYSTEM']
        channels = ['EMAIL', 'PUSH', 'SMS']

        settings = []
        for user_id in user_ids:
            for noti_type in notification_types:
                for channel in channels:
                    is_enabled = self.fake.boolean(chance_of_getting_true=70)
                    settings.append({
                        'user_id': user_id,
                        'notification_type': noti_type,
                        'channel': channel,
                        'is_enabled': is_enabled,
                        'created_at': datetime.now(),
                        'updated_at': datetime.now()
                    })

        sql = """
            INSERT INTO notification_setting (user_id, notification_type, channel,
                                              is_enabled, created_at, updated_at)
            VALUES (%(user_id)s, %(notification_type)s, %(channel)s,
                    %(is_enabled)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, settings, "알림 설정")

    def generate_all(self, user_count=50):
        self.generate_users(user_count)
