import styled from "styled-components";

export const ContainerLayout = styled.div`
  width: 100%;
  background-color: #f8f8f8;
`;

export const HeaderBox = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0; 
`;

export const SearchInput = styled.input`
  padding: 8px;
  width: 90%;
  max-width: 400px; 
  border: 1px solid #ccc;
  border-radius: 19px;
  font-size: 14px; 
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); 
  transition: border-color 0.3s; 

  &:focus {
    border-color: #007bff; 
    outline: none; 
  }
`;
