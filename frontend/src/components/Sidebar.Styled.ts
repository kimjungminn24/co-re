import styled from "styled-components";

export const MainLogo = styled.img`
    padding-top: 15px;
    width: 120px;
    height: auto;
`
export const SidebarLayout = styled.div`
    display: flex;
    justify-items: center;
    align-items: center;
    flex-direction: column;
    height: 100vh; /* 전체 화면 높이로 설정 */
    width: 240px;
`
export const ButtonLayout = styled.div`
    display: flex;
    flex-direction: column;
    margin-top: 30px;
    width: 100%;
    height: 100%; /* 버튼 레이아웃을 전체 높이로 설정 */
    gap: 10px;
`
export const ButtonBottomGroup = styled.div`
    margin-top: auto; /* 최하단으로 설정 */
    margin-bottom: 50px;
    display: flex;
    flex-direction: column;
    gap: 10px;
`
export const Divider = styled.hr`
    width: 100%;
    border: none;
    border-top: 0.6px solid #E0E0E0;
    margin: 20px 0; /* 상하 간격 */
`;