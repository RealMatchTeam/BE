from faker import Faker

fake = Faker('ko_KR')


class BaseGenerator:
    def __init__(self, connection):
        self.connection = connection
        self.fake = fake

    def execute_many(self, sql, data, entity_name="데이터"):
        if not data:
            print(f"[경고] 생성할 {entity_name}가 없습니다.")
            return

        with self.connection.cursor() as cursor:
            cursor.executemany(sql, data)
            self.connection.commit()
            print(f"[완료] {len(data)}개의 {entity_name} 생성 완료")
