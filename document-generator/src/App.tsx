import { BrowserRouter, Routes, Route } from "react-router-dom";
import { MainLayout } from "./layout/MainLayout";
import { HomePage } from "./pages/HomePage";
import { CustomersPage } from "./pages/CustomerPage";
import { CreateCustomerPage } from "./pages/CreateCustomerPage";
import { EditCustomerPage } from "./pages/EditCustomerPage";
import { DocumentsPage } from "./pages/DocumentsPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/customers" element={<CustomersPage />} />
          <Route path="/customers/create" element={<CreateCustomerPage />} />
          <Route path="/customers/edit/:id" element={<EditCustomerPage />} />
          <Route path="/documents" element={<DocumentsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
