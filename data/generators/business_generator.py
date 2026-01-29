from .base_generator import BaseGenerator


class BusinessGenerator(BaseGenerator):
    def generate_all(self, applies_per_campaign=3):
        print(f"\n[비즈니스 도메인] FK 관계로 인해 스킵합니다.")
