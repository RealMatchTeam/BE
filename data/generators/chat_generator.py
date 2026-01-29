from .base_generator import BaseGenerator


class ChatGenerator(BaseGenerator):
    def generate_all(self, room_count=20, messages_per_room=10):
        print(f"\n[채팅 도메인] FK 관계로 인해 스킵합니다.")
