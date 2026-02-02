import random
from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class CampaignGenerator(BaseGenerator):
    # 한국어 캠페인 데이터 템플릿
    CAMPAIGN_TITLES = [
        "신제품 체험단 모집",
        "브랜드 홍보 크리에이터 모집",
        "뷰티 제품 리뷰 캠페인",
        "패션 아이템 협찬 캠페인",
        "푸드 콘텐츠 제작 캠페인",
        "라이프스타일 콘텐츠 협업",
        "신규 런칭 제품 홍보 캠페인",
        "시즌 한정 프로모션 캠페인",
        "브랜드 앰버서더 모집",
        "SNS 바이럴 마케팅 캠페인",
        "유튜브 리뷰어 모집",
        "인스타그램 피드 협찬",
        "틱톡 숏폼 제작 캠페인",
        "블로그 체험단 모집",
        "언박싱 콘텐츠 제작",
        "제품 사용 후기 캠페인",
        "브이로그 협찬 캠페인",
        "스타일링 콘텐츠 모집",
        "레시피 콘텐츠 제작",
        "여행 콘텐츠 협업 캠페인"
    ]

    CAMPAIGN_DESCRIPTIONS = [
        "안녕하세요! 저희 브랜드의 신제품을 소개해 주실 크리에이터분들을 모집합니다. 진정성 있는 콘텐츠 제작이 가능하신 분들의 많은 참여 부탁드립니다. 제품 협찬과 함께 리워드가 지급됩니다.",
        "이번 시즌 새롭게 출시되는 제품의 홍보를 위한 캠페인입니다. 크리에이터님의 개성 있는 콘텐츠로 제품의 특징을 잘 보여주시면 됩니다. 자유로운 콘텐츠 기획이 가능합니다.",
        "저희 브랜드와 함께할 인플루언서를 찾고 있습니다. 팔로워 수보다는 콘텐츠 퀄리티와 소통력을 중요하게 생각합니다. 장기적인 파트너십도 고려하고 있으니 많은 관심 부탁드립니다.",
        "SNS에서 활발하게 활동 중인 크리에이터분들과 협업하고 싶습니다. 제품을 직접 사용해보시고 솔직한 후기를 남겨주시면 됩니다. 창의적인 콘텐츠 제작을 환영합니다.",
        "일상 속에서 자연스럽게 제품을 노출해 주실 크리에이터를 모집합니다. 억지스러운 광고보다는 진솔한 리뷰를 선호합니다. 콘텐츠 가이드라인은 별도로 안내드립니다.",
        "저희 제품을 활용한 다양한 콘텐츠를 만들어 주실 분들을 찾습니다. 사진, 영상 등 콘텐츠 형식은 자유입니다. 크리에이터님의 스타일을 존중하며 협업하겠습니다.",
        "브랜드 인지도 향상을 위한 마케팅 캠페인에 참여해 주실 크리에이터를 모집합니다. 제품 특성에 맞는 타겟 오디언스를 보유하신 분들의 지원을 기다립니다.",
        "영상 콘텐츠 제작에 능숙하신 크리에이터분들을 찾습니다. 제품 언박싱, 사용 후기 등 다양한 형식의 영상이 가능합니다. 촬영 장비와 편집 스킬을 갖추신 분 환영합니다.",
        "뷰티/패션 분야에서 활동 중인 크리에이터와 협업하고 싶습니다. 트렌디한 콘텐츠로 저희 브랜드를 표현해 주세요. 스타일리시한 콘텐츠를 기대합니다.",
        "제품의 실제 사용 경험을 공유해 주실 분들을 모십니다. 사용 전/후 비교, 장단점 분석 등 구체적인 리뷰를 환영합니다. 진정성 있는 콘텐츠를 추구합니다."
    ]

    PREFERRED_SKILLS = [
        "영상 편집", "사진 촬영", "콘텐츠 기획", "스토리텔링",
        "SNS 마케팅", "브이로그 제작", "제품 리뷰", "푸드 스타일링",
        "패션 스타일링", "뷰티 메이크업", "음식 촬영", "여행 콘텐츠",
        "라이프스타일", "인테리어 촬영", "ASMR 제작", "숏폼 콘텐츠",
        "릴스 제작", "유튜브 쇼츠", "틱톡 영상", "카드뉴스 제작",
        "썸네일 디자인", "자막 편집", "색보정", "모션 그래픽"
    ]

    PRODUCTS = [
        "스킨케어 세트", "메이크업 팔레트", "헤어 에센스", "향수",
        "운동복 세트", "액세서리", "가방", "신발",
        "건강식품", "다이어트 보조제", "단백질 쉐이크", "비타민",
        "주방용품", "인테리어 소품", "생활용품", "전자제품",
        "간편식", "음료", "스낵", "디저트",
        "반려동물 용품", "유아용품", "문구류", "도서"
    ]

    def generate_campaigns(self, count=30):
        print(f"\n[캠페인] {count}개의 캠페인 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'BRAND'")
            user_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not user_ids:
            print("[경고] BRAND 역할의 사용자가 없습니다.")
            return

        if not brand_ids:
            print("[경고] 브랜드가 없습니다.")
            return

        campaigns = []
        for i in range(count):
            start_date = self.fake.date_between(start_date='today', end_date='+60d')
            end_date = start_date + timedelta(days=self.fake.random_int(30, 90))
            recruit_start = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
            recruit_end = recruit_start + timedelta(days=self.fake.random_int(14, 30))

            # 한국어 데이터 생성
            title = random.choice(self.CAMPAIGN_TITLES)
            description = random.choice(self.CAMPAIGN_DESCRIPTIONS)
            preferred_skills = ', '.join(random.sample(self.PREFERRED_SKILLS, 3))
            product = random.choice(self.PRODUCTS)

            campaign = {
                'title': title,
                'description': description,
                'preferred_skills': preferred_skills,
                'schedule': f"주 {self.fake.random_int(1, 3)}회 콘텐츠 제작 및 업로드",
                'video_spec': f"{self.fake.random_element(['세로형', '가로형'])} {self.fake.random_element(['30초', '1분', '3분'])} 영상",
                'product': product,
                'reward_amount': self.fake.random_element([500000, 1000000, 1500000, 2000000, 3000000]),
                'start_date': start_date,
                'end_date': end_date,
                'recruit_start_date': recruit_start,
                'recruit_end_date': recruit_end,
                'quota': self.fake.random_int(5, 30),
                'brand_id': self.fake.random_element(brand_ids),
                'created_by': self.fake.random_element(user_ids),
                'is_deleted': False,
                'created_at': datetime.now() - timedelta(days=self.fake.random_int(10, 60)),
                'updated_at': datetime.now() - timedelta(days=self.fake.random_int(0, 10))
            }
            campaigns.append(campaign)

        sql = """
            INSERT INTO campaign (title, description, preferred_skills, schedule,
                                  video_spec, product, reward_amount, start_date,
                                  end_date, recruit_start_date, recruit_end_date,
                                  quota, brand_id, created_by, is_deleted, created_at, updated_at)
            VALUES (%(title)s, %(description)s, %(preferred_skills)s, %(schedule)s,
                    %(video_spec)s, %(product)s, %(reward_amount)s, %(start_date)s,
                    %(end_date)s, %(recruit_start_date)s, %(recruit_end_date)s,
                    %(quota)s, %(brand_id)s, %(created_by)s, %(is_deleted)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, campaigns, "캠페인")

    def generate_campaign_likes(self):
        print(f"\n[캠페인 좋아요] 캠페인 좋아요 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign")
            campaign_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        if not campaign_ids or not creator_ids:
            print("[경고] 캠페인 또는 크리에이터가 없습니다.")
            return

        likes = []
        for creator_id in creator_ids:
            liked_campaigns = self.fake.random_sample(campaign_ids, length=self.fake.random_int(0, min(5, len(campaign_ids))))
            for campaign_id in liked_campaigns:
                likes.append({
                    'user_id': creator_id,
                    'campaign_id': campaign_id,
                    'created_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                })

        if likes:
            sql = """
                INSERT INTO campaign_like (user_id, campaign_id, created_at)
                VALUES (%(user_id)s, %(campaign_id)s, %(created_at)s)
            """
            self.execute_many(sql, likes, "캠페인 좋아요")

    def generate_campaign_content_tags(self):
        """캠페인 콘텐츠 태그 연결 (모든 캠페인)"""
        print(f"\n[캠페인 콘텐츠 태그] 캠페인 콘텐츠 태그 연결 중...")

        with self.connection.cursor() as cursor:
            # 모든 캠페인 ID 조회
            cursor.execute("SELECT id FROM campaign")
            campaigns = cursor.fetchall()
            print(f"  - 조회된 캠페인 수: {len(campaigns) if campaigns else 0}")
            if not campaigns:
                print("[경고] 캠페인이 없습니다.")
                return
            campaign_ids = [c['id'] for c in campaigns]

            # 태그 조회
            cursor.execute("SELECT id, tag_type, eng_name FROM tag_content")
            tags = cursor.fetchall()
            print(f"  - 조회된 태그 수: {len(tags) if tags else 0}")
            if not tags:
                print("[경고] tag_content 테이블에 데이터가 없습니다.")
                return

        # 태그를 타입별로 그룹화
        tags_by_type = {}
        for tag in tags:
            tag_type = tag['tag_type']
            if tag_type not in tags_by_type:
                tags_by_type[tag_type] = []
            tags_by_type[tag_type].append(tag)

        # ETC가 아닌 태그만 필터링
        non_etc_tags_by_type = {}
        for tag_type, tag_list in tags_by_type.items():
            non_etc_tags_by_type[tag_type] = [t for t in tag_list if t['eng_name'] != 'ETC']

        data = []
        custom_values = ['커스텀 콘텐츠', '특별 기획', '브랜디드 콘텐츠', '콜라보 영상']

        for campaign_id in campaign_ids:
            # 각 캠페인에 타입별로 1~2개씩 태그 연결
            for tag_type, tag_list in non_etc_tags_by_type.items():
                if not tag_list:
                    continue

                # 랜덤하게 1~2개 태그 선택
                num_tags = min(self.fake.random_int(1, 2), len(tag_list))
                selected_tags = random.sample(tag_list, num_tags)

                for tag in selected_tags:
                    data.append({
                        'campaign_id': campaign_id,
                        'content_tag_id': tag['id'],
                        'custom_tag_value': None,
                        'created_at': datetime.now(),
                        'updated_at': datetime.now()
                    })

            # 20% 확률로 ETC 태그 추가 (커스텀 값 포함)
            if self.fake.random_int(1, 100) <= 20:
                for tag_type, tag_list in tags_by_type.items():
                    etc_tags = [t for t in tag_list if t['eng_name'] == 'ETC']
                    if etc_tags:
                        data.append({
                            'campaign_id': campaign_id,
                            'content_tag_id': etc_tags[0]['id'],
                            'custom_tag_value': random.choice(custom_values),
                            'created_at': datetime.now(),
                            'updated_at': datetime.now()
                        })
                        break  # 하나만 추가

        if data:
            sql = """
                INSERT IGNORE INTO campaign_content_tag (campaign_id, content_tag_id, custom_tag_value, created_at, updated_at)
                VALUES (%(campaign_id)s, %(content_tag_id)s, %(custom_tag_value)s, %(created_at)s, %(updated_at)s)
            """
            self.execute_many(sql, data, "캠페인 콘텐츠 태그")

    def generate_all(self, campaign_count=30):
        self.generate_campaigns(campaign_count)
        self.generate_campaign_likes()
        self.generate_campaign_content_tags()
