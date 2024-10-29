# import streamlit as st
# import folium
# from folium.plugins import MarkerCluster
#
# import pandas as pd
# from streamlit_folium import st_folium
#
# # 제목
# st.title("도서관 위치")
#
# # Excel 파일 로드
# # data = pd.read_excel('C:/FrontEnd/new2/BookProject/src/main/python/map.xlsx')
# # data = pd.read_excel('src/main/python/map.xlsx')
# data = pd.read_csv('src/main/python/map.csv')
# # 결측치 처리 (예: 결측치가 있는 행 삭제)
# data = data.dropna(subset=['LBRRY_LA', 'LBRRY_LO'])
#
# # Folium 지도 생성 (기본 위치는 서울)
# m = folium.Map(location=[37.5665, 126.9780], zoom_start=12)
#
# # MarkerCluster 객체 생성
# marker_cluster = MarkerCluster().add_to(m)
#
# # 데이터에서 마커 추가
# for idx, row in data.iterrows():
#     # 마커 생성
#     marker = folium.Marker(
#         location=[row['LBRRY_LA'], row['LBRRY_LO']],
#         icon=folium.Icon(color="blue", icon="info-sign")
#     )
#
#     # 팝업 내용 설정
#     popup_content = f"""
#     <strong>{row['NAME']}</strong><br>
#     주소: {row['ADDR']}<br>
#     전화: {row['TEL']}
#     """
#
#     # 팝업 추가
#     marker.add_child(folium.Popup(popup_content, max_width=300))
#
#     # MarkerCluster에 마커 추가
#     marker.add_to(marker_cluster)
#
# # Streamlit에 Folium 지도 표시
# st_folium(m, width=700)

import streamlit as st
import folium
from folium.plugins import MarkerCluster
import pandas as pd
from streamlit_folium import st_folium
from geopy.distance import great_circle

# 제목
st.title("도서관 위치")

# 사용자의 현재 위치 (위도, 경도)
user_location = (37.5665, 126.9780)  # 예: 서울시청 위치 (위도, 경도)

# 초기 데이터 로드
data = pd.read_csv('src/main/python/map.csv')

# 결측치 처리
data = data.dropna(subset=['LBRRY_LA', 'LBRRY_LO'])

# Folium 지도 생성 (기본 위치는 서울)
m = folium.Map(location=user_location, zoom_start=12)

# 마커 추가 함수
def add_markers(map_obj, location):
    # MarkerCluster 객체 생성
    marker_cluster = MarkerCluster().add_to(map_obj)

    # 도서관과 사용자의 거리 계산
    data['distance'] = data.apply(lambda row: great_circle(location, (row['LBRRY_LA'], row['LBRRY_LO'])).meters, axis=1)

    # 가장 가까운 30개의 도서관 선택
    nearest_libraries = data.nsmallest(30, 'distance')

    for idx, row in nearest_libraries.iterrows():
        # 마커 생성
        marker = folium.Marker(
            location=[row['LBRRY_LA'], row['LBRRY_LO']],
            icon=folium.Icon(color="blue", icon="info-sign")
        )
        # 팝업 내용 설정
        popup_content = f"""
        <strong>{row['NAME']}</strong><br>
        주소: {row['ADDR']}<br>
        전화: {row['TEL']}<br>
        거리: {row['distance']:.2f} m
        """
        marker.add_child(folium.Popup(popup_content, max_width=300))
        marker.add_to(marker_cluster)

# 초기 마커 추가
add_markers(m, user_location)

# Streamlit에 Folium 지도 표시 및 중심 좌표 반환
map_result = st_folium(m, width=700, returned_objects=["m", "f"])

# 지도 이동 후 중심 좌표 가져오기
if map_result and 'm' in map_result and 'center' in map_result['m']:
    center_location = map_result['m']['center']
    st.write("현재 중심 좌표:", center_location)

    # 중심 좌표를 기준으로 도서관 데이터 재로드
    new_location = (center_location['lat'], center_location['lng'])

    # 새로운 마커 추가
    m = folium.Map(location=new_location, zoom_start=12)  # 새로운 지도 객체 생성
    add_markers(m, new_location)

    # Streamlit에 Folium 지도 표시 (갱신된)
    st_folium(m, width=700)

