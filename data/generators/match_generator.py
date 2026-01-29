from .base_generator import BaseGenerator


class MatchGenerator(BaseGenerator):
    def generate_all(self):
        print(f"\n[매칭 도메인] FK 관계로 인해 스킵합니다.")
