import { createContext, useContext, useState } from "react";

const PageHeaderContext = createContext();

export function PageHeaderProvider({ children }) {
  const [header, setHeader] = useState({ title: "", breadcrumb: "" });
  return (
    <PageHeaderContext.Provider value={{ header, setHeader }}>
      {children}
    </PageHeaderContext.Provider>
  );
}

export const usePageHeader = () => useContext(PageHeaderContext);