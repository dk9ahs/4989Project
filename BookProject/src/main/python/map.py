import streamlit as st
import folium
from folium.plugins import MarkerCluster

import pandas as pd
from streamlit_folium import st_folium

# 제목
st.title("도서관 위치")

# Excel 파일 로드
# data = pd.read_excel('C:/FrontEnd/new2/BookProject/src/main/python/map.xlsx')
# data = pd.read_excel('src/main/python/map.xlsx')
data = pd.read_csv('src/main/python/map.csv')
# 결측치 처리 (예: 결측치가 있는 행 삭제)
data = data.dropna(subset=['LBRRY_LA', 'LBRRY_LO'])

# Folium 지도 생성 (기본 위치는 서울)
m = folium.Map(location=[37.5665, 126.9780],
               zoom_start=12)

# MarkerCluster 객체 생성
marker_cluster = MarkerCluster().add_to(m)

# 데이터에서 마커 추가
for idx, row in data.iterrows():
    # 마커 생성
    marker = folium.Marker(
        location=[row['LBRRY_LA'], row['LBRRY_LO']],
        icon=folium.Icon(color="red", icon="star")
    )

    # 팝업 내용 설정
    popup_content = f"""
                    <strong>{row['NAME']}</strong><br>
                    주소: {row['ADDR']}<br>
                    전화: {row['TEL']}
                    """

    # 팝업 추가
    marker.add_child(folium.Popup(popup_content, max_width=300))

    # MarkerCluster에 마커 추가
    marker.add_to(marker_cluster)

# Streamlit에 Folium 지도 표시
st_folium(m, width=700)

